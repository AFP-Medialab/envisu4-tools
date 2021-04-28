package com.afp.medialab.envisu4.tools.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Optional;
import java.util.Set;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.afp.medialab.envisu4.tools.controller.exception.AnimatedGifCreationException;
import com.afp.medialab.envisu4.tools.controller.exception.Envisu4ServiceError;
import com.afp.medialab.envisu4.tools.controller.exception.ServiceErrorCode;
import com.afp.medialab.envisu4.tools.controller.models.CreateAnimatedGifRequest;
import com.afp.medialab.envisu4.tools.dao.entities.Image;
import com.afp.medialab.envisu4.tools.dao.repository.ImageDbRepository;
import com.afp.medialab.envisu4.tools.images.AnimatedGIFWriter;

@RestController
public class AnimatedGifController {

	@Autowired
	private ImageDbRepository imageDbRepository;

	private static Logger Logger = LoggerFactory.getLogger(AnimatedGifController.class);

	// @ApiOperation(value = "Create animated Gif")
	@RequestMapping(path = "/createAnimated", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.IMAGE_GIF_VALUE)
	public Resource createAnimatedGifForURL(@RequestBody @Valid CreateAnimatedGifRequest createAnimatedGifRequest)
			throws Exception {

		Set<String> urls = createAnimatedGifRequest.getInputURLs();
		int delay = createAnimatedGifRequest.getDelay();
		AnimatedGIFWriter writer = new AnimatedGIFWriter(true);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			writer.prepareForWrite(output, -1, -1);
			for (String url : urls) {
				Logger.debug("images url {}", url);
				BufferedImage next = ImageIO.read(new URL(url));
				writer.writeFrame(output, next, delay);
			}
			byte[] content = output.toByteArray();
			String md5sum = DigestUtils.md5Hex(content);
			Optional<Image> storeImage = imageDbRepository.findByMd5sum(md5sum);
			if (storeImage.isEmpty()) {
				Image image = new Image();
				image.setContent(content);
				image.setMd5sum(md5sum);
				image.setCreationDate(Calendar.getInstance().getTime());
				imageDbRepository.save(image);
			}

			return new ByteArrayResource(content);

		} catch (IIOException e) {
			Logger.error("IIOException", e);
			throw new AnimatedGifCreationException(ServiceErrorCode.ANIMATED_GIF_URL_LOAD_FAILED,
					"Fail loading input resource");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.error("General error", e);
			throw new AnimatedGifCreationException(ServiceErrorCode.ANIMATED_GIF_CREATION_FAILED,
					"Error creating image ");
		} finally {
			writer.finishWrite(output);
			output.close();
		}
	}

	@ExceptionHandler({ AnimatedGifCreationException.class })
	public ResponseEntity<Envisu4ServiceError> handle(AnimatedGifCreationException ex) {
		return new ResponseEntity<Envisu4ServiceError>(ex.getIpolServiceError(), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
