package it.polimi.tiw.albums.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.http.Part;
import net.coobird.thumbnailator.Thumbnails;

public class MediaManager{
	
	public static String saveImageToSystem(Part image, String uploadPath) {
		UUID fileUUID = null;
		String fileName = "";
		File f = null;
				
		try(InputStream iStream = image.getInputStream()){
			byte[] imageBytes = IOUtils.toByteArray(iStream);
			do {
			//Generate uuid from image's bytes
			fileUUID = UUID.nameUUIDFromBytes(imageBytes);
			fileName = fileUUID.toString();
			fileName = fileName.concat(".png");
			//Check if image already exists
			f = new File(uploadPath+fileName);
			
			//Change byte array so that if file already exists next iteration will get
			//different UUID
			//TODO change how byteArray is modified to re-use more of the original bytes.
			new Random().nextBytes(imageBytes);
			}while(f.isFile());
			
			//Re-write image to png extension and write to disk
			BufferedImage originalImage = ImageIO.read(image.getInputStream());
			ImageIO.write(originalImage, "png", f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return fileName;
	}

	public static boolean generateThumbnailFromFile(File file, String thumbnailName, String thumbnailPath) {
		try {
			Thumbnails.of(file)
				.size(256, 256)
				.toFile(thumbnailPath+thumbnailName);
			
			return true;
		} catch (IOException e) {
			System.out.println("Exception while generating thumbnail for file: "+file);
			e.printStackTrace();
		}
		
		return false;
	}

}





