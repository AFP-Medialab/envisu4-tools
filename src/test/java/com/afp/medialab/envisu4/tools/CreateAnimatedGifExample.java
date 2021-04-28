package com.afp.medialab.envisu4.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.codec.cli.Digest;
import org.apache.commons.codec.digest.DigestUtils;

import com.afp.medialab.envisu4.tools.images.AnimatedGIFWriter;

public class CreateAnimatedGifExample {

	/*private static String path1 = "/Users/bertrand/Documents/travail/afp/Projets/envisu4/test/output_0.png";
	private static String path2 = "/Users/bertrand/Documents/travail/afp/Projets/envisu4/test/output_1.png";
	private static String path3 = "/Users/bertrand/Documents/travail/afp/Projets/envisu4/test/pano.jpeg";*/
	private static String path1 = "https://ipolcore.ipol.im//api/core/shared_folder/run/77777000125/A05806C69EA5E00472BD2FDC4EA3FE8B/output_0.png";
	private static String path2 = "https://ipolcore.ipol.im//api/core/shared_folder/run/77777000125/A05806C69EA5E00472BD2FDC4EA3FE8B/output_1.png";
	private static String path3 = "https://ipolcore.ipol.im//api/core/shared_folder/run/77777000125/A05806C69EA5E00472BD2FDC4EA3FE8B/pano.jpg";
	private static String animatedGif = "/Users/bertrand/Documents/travail/afp/Projets/envisu4/test/animated_gif_2.gif";

	public static void main(String[] args) throws Exception {
		
		
		OutputStream output = new FileOutputStream(new File(animatedGif));
		
		AnimatedGIFWriter writer = new AnimatedGIFWriter(false);
		writer.prepareForWrite(output, -1, -1);

		
		URL[] urls = new URL[] {new URL(path1), new URL(path2), new URL(path3)}; 

		for (URL image : urls) {
			BufferedImage next = ImageIO.read(image);
			writer.writeFrame(output, next, 300);
		}

		writer.finishWrite(output);
		output.close();
		
		String md5sum = DigestUtils.md5Hex(new FileInputStream(animatedGif));
		System.out.println(md5sum);

	}

}
