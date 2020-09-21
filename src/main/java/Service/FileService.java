package Service;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class FileService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hi")
    public String sayHello(){
        System.out.println("lol");
        return "com.pace.Hello,I am text!";
    }

    @POST
    @Path("/fileupload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @HeaderParam("content-length") long contentLength,
            @FormDataParam("email") String email,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        String uploadedFileLocation = "./UploadedFiles/" + fileDetail.getFileName();
        System.out.println(contentLength);
        if(contentLength > 58805000){
            return Response.status(400).entity("File size exceeded limit 56Mo").build();
        }
        File objFile=new File(uploadedFileLocation);
        if(objFile.exists())
            objFile.delete();
        String response;
        try {
            saveToFile(uploadedInputStream, uploadedFileLocation);
            response = "File uploaded to: " + uploadedFileLocation;
        } catch (IOException e) {
            e.printStackTrace();
            response = "File could not be uploaded to: " + uploadedFileLocation;
        }

        return Response.status(200).entity(response).build();

    }
    private void saveToFile(InputStream uploadedInputStream,
                            String uploadedFileLocation) throws IOException {
        try {
            OutputStream out;
            int read;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Below this line is client test code (android client)

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/upload")
    public String TestUpload() throws Exception {
        File file = new File("C:\\ts.zip");
        if(file.exists()){
            String requestURL = "http://localhost:8080/fileupload";

            Map<String, String> params = new HashMap<>(2);
            params.put("email", "o@gmail.com");

            String result = multipartRequest(requestURL, params, file.getAbsolutePath(), "file", "application/zip");
        }
        return "ok";
    }

    public String multipartRequest(String urlTo, Map<String, String> params, String filepath, String filefield, String fileMimeType) throws Exception {
        HttpURLConnection connection;
        DataOutputStream outputStream;
        InputStream inputStream;

        String twoHyphens = "--";
        String boundary = "*****" + System.currentTimeMillis() + "*****";
        String lineEnd = "\r\n";

        String result;

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;

        String[] q = filepath.split("\\\\");
        int idx = q.length - 1;

        try {
            File file = new File(filepath);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL url = new URL(urlTo);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" + q[idx] + "\"" + lineEnd);
            outputStream.writeBytes("Content-Type: " + fileMimeType + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);

            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);

            // Upload POST Data
            for (String key : params.keySet()) {
                String value = params.get(key);

                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                outputStream.writeBytes("Content-Type: text/plain" + lineEnd);
                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(value);
                outputStream.writeBytes(lineEnd);
            }

            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


            if (200 != connection.getResponseCode()) {
                throw new Exception("Failed to upload code:" + connection.getResponseCode() + " " + connection.getResponseMessage());
            }

            inputStream = connection.getInputStream();

            result = this.convertStreamToString(inputStream);

            fileInputStream.close();
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return result;
        } catch (Exception e) {
            throw new Exception(e);
        }

    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
