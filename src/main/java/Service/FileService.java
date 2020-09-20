package Service;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartMediaTypes;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.*;

@Path("/")
public class FileService {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hi")
    public String sayHello(){
        return "com.pace.Hello,I am text!";
    }

    @POST
    @Path("/fileupload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @HeaderParam("content-length") long contentLength,
            @DefaultValue("true") @FormDataParam("enabled") boolean enabled,
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        String uploadedFileLocation = "C://" + fileDetail.getFileName();
        System.out.println(uploadedFileLocation);
        File objFile=new File(uploadedFileLocation);
        if(objFile.exists())
        {
            objFile.delete();
        }

        saveToFile(uploadedInputStream, uploadedFileLocation);

        String output = "File uploaded to: " + uploadedFileLocation;

        return Response.status(200).entity(output).build();

    }
    private void saveToFile(InputStream uploadedInputStream,
                            String uploadedFileLocation) {
        try {
            OutputStream out;
            int read;
            byte[] bytes = new byte[10485760];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }
}
