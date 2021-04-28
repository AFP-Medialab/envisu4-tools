package com.afp.medialab.envisu4.tools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import com.afp.medialab.envisu4.tools.images.GifSequenceWriter;

public class CreateGifExample {

	/*private static String path1 = "/Users/bertrand/Documents/travail/afp/Projets/envisu4/test/output_0.png";
	private static String path2 = "/Users/bertrand/Documents/travail/afp/Projets/envisu4/test/output_1.png";
	private static String path3 = "/Users/bertrand/Documents/travail/afp/Projets/envisu4/test/pano.jpeg";*/
	private static String path1 = "https://ipolcore.ipol.im//api/core/shared_folder/run/77777000125/A05806C69EA5E00472BD2FDC4EA3FE8B/output_0.png";
	private static String path2 = "https://ipolcore.ipol.im//api/core/shared_folder/run/77777000125/A05806C69EA5E00472BD2FDC4EA3FE8B/output_1.png";
	private static String path3 = "https://ipolcore.ipol.im//api/core/shared_folder/run/77777000125/A05806C69EA5E00472BD2FDC4EA3FE8B/pano.jpg";
	private static String animatedGif = "/Users/bertrand/Documents/travail/afp/Projets/envisu4/test/animated_gif.gif";

	public static void main(String[] args) throws Exception {
		
		BufferedImage first = ImageIO.read(new URL(path2));
		ImageOutputStream output = new FileImageOutputStream(new File(animatedGif));

		GifSequenceWriter writer = new GifSequenceWriter(output, first.getType(), 350, true);
		//writer.writeToSequence(first);

		File[] images = new File[] { new File(path1), new File(path2), new File(path3),};
		URL[] urls = new URL[] {new URL(path1), new URL(path2), new URL(path3)}; 

		for (URL image : urls) {
			BufferedImage next = ImageIO.read(image);
			writer.writeToSequence(next);
		}

		writer.close();
		output.close();

	}

}
