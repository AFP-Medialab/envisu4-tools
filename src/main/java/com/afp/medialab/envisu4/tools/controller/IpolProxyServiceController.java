package com.afp.medialab.envisu4.tools.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.afp.medialab.envisu4.tools.constraints.IpolResultEnum;
import com.afp.medialab.envisu4.tools.controller.exception.Envisu4ServiceError;
import com.afp.medialab.envisu4.tools.controller.exception.IpolProxyException;
import com.afp.medialab.envisu4.tools.controller.exception.ServiceErrorCode;

@RestController
public class IpolProxyServiceController {

	private static Logger Logger = LoggerFactory.getLogger(IpolProxyServiceController.class);

	@Value("${application.ipol.endpoint}")
	private String ipolEndpoint;

	private static String HOMOGRAPHIC_ENDPOINT = "/api/core/run";
	private static String RESULT_ENDPOINT = "/api/core/shared_folder/run/77777000125/";

	private static String CLIENT_DATA = "{\"demo_id\": 77777000125, \"origin\": \"upload\", \"params\": {}}";

	@Autowired
	@Qualifier("restTemplate")
	private RestTemplate restTemplate;

	/**
	 * Call ipol service from files
	 * 
	 * @param file_0
	 * @param file_1
	 * @return
	 */
	//@ApiOperation(value = "Call IPOL homographic service with uploaded files")
	@RequestMapping(path = "/ipol/homographic", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> homographicProcessCall(@RequestParam("file_0") MultipartFile file_0,
			@RequestParam("file_1") MultipartFile file_1) {
		FileSystemResource fileSysRs0 = new FileSystemResource(convert(file_0));
		FileSystemResource fileSysRs1 = new FileSystemResource(convert(file_1));
		return callAndBuildIpolService(fileSysRs0, fileSysRs1, CLIENT_DATA);
	}

	/**
	 * Call ipol service from URL
	 * 
	 * @param url_0
	 * @param url_1
	 * @return
	 * @throws IpolProxyException
	 */
	//@ApiOperation(value = "Call IPOL homographic service with url resources")
	@RequestMapping(path = "/ipol/homographic", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<JSONObject> homographicProcessCall(@RequestParam("url_0") String url_0,
			@RequestParam("url_1") String url_1) throws IpolProxyException {
		try {
			URL url0 = new URL(url_0);
			URL url1 = new URL(url_1);
			FileSystemResource fileSysRs0 = new FileSystemResource(convert(url0));
			FileSystemResource fileSysRs1 = new FileSystemResource(convert(url1));
			return callAndBuildIpolService(fileSysRs0, fileSysRs1);
		} catch (MalformedURLException e) {
			Logger.error("Bad url format", e);
			throw new IpolProxyException(ServiceErrorCode.IPOL_BAD_URL_ERROR, e.getMessage());
		} catch (IOException e) {
			Logger.error("Error download image from url", e);
			throw new IpolProxyException(ServiceErrorCode.IPOL_FILE_IMAGE_ERROR, e.getMessage());
		}
	}

	//@ApiOperation(value = "Get images results from IPOL service")
	@RequestMapping(path = "/ipol/homographic/{key}/{image}", method = RequestMethod.GET, produces = {
			MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE, MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<Resource> imagepng(@PathVariable String key, @PathVariable IpolResultEnum image)
			throws IpolProxyException {
		Logger.debug("key {}", key);
		Logger.debug("image {}", image);
		try {
			ResponseEntity<Resource> response = restTemplate
					.getForEntity(ipolEndpoint + RESULT_ENDPOINT + key + "/" + image.getCode(), Resource.class);
			return response;
		} catch (HttpClientErrorException e) {

			switch (e.getRawStatusCode()) {
			case 404: {
				throw new IpolProxyException(ServiceErrorCode.IPOL_RESOURCE_NOT_FOUND,
						"Not resource found in IPOL Server with key " + key);
			}
			default:
				throw new IpolProxyException(ServiceErrorCode.IPOL_REMOTE_SERVICE_ERROR, e.getMessage());
			}

		}

	}

	/**
	 * Call ipol service with static clientData value
	 * 
	 * @param fileSysRs0
	 * @param fileSysRs1
	 * @return
	 */
	private ResponseEntity<JSONObject> callAndBuildIpolService(FileSystemResource fileSysRs0,
			FileSystemResource fileSysRs1) {
		return callAndBuildIpolService(fileSysRs0, fileSysRs1, CLIENT_DATA);
	}

	/**
	 * Call ipol service with tempory images
	 * 
	 * @param fileSysRs0
	 * @param fileSysRs1
	 * @param clientData
	 * @return
	 */
	private ResponseEntity<JSONObject> callAndBuildIpolService(FileSystemResource fileSysRs0,
			FileSystemResource fileSysRs1, String clientData) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("file_0", fileSysRs0);
		body.add("file_1", fileSysRs1);
		body.add("clientData", clientData);
		HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
		ResponseEntity<JSONObject> response = restTemplate.postForEntity(ipolEndpoint + HOMOGRAPHIC_ENDPOINT,
				requestEntity, JSONObject.class);
		JSONObject respBody = response.getBody();
		ResponseEntity<JSONObject> responseEntity = new ResponseEntity<JSONObject>(respBody, response.getStatusCode());
		return responseEntity;
	}

	/**
	 * Create temporary images from url
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 * @throws IpolProxyException
	 */
	private static File convert(URL url) throws IOException, IpolProxyException {
		ImageInputStream iis = null;
		try {
			iis = ImageIO.createImageInputStream(url.openStream());
			Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
			if (imageReaders.hasNext()) {
				ImageReader reader = (ImageReader) imageReaders.next();
				reader.setInput(iis);
				BufferedImage img = reader.read(0);
				String format = reader.getFormatName();
				Logger.debug("formatName: {}", format);
				String md5sumFileName = DigestUtils.md5Hex(url.getFile());
				File convFile = new File("temp_image", md5sumFileName + "." + format.toLowerCase());
				ImageIO.write(img, format, convFile);
				return convFile;

			} else {
				throw new IpolProxyException(ServiceErrorCode.IPOL_IMAGE_NOT_FOUND,
						"No Image found in URL " + url.toString());
			}

		} finally {
			if (iis != null)
				iis.close();
		}

	}

	/**
	 * Create temporary file
	 * 
	 * @param file
	 * @return
	 */
	private static File convert(MultipartFile file) {
		File convFile = new File("temp_image", file.getOriginalFilename());
		if (!convFile.getParentFile().exists()) {
			System.out.println("mkdir:" + convFile.getParentFile().mkdirs());
		}
		try {
			convFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(file.getBytes());
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convFile;
	}

	@ExceptionHandler({ IpolProxyException.class })
	public ResponseEntity<Envisu4ServiceError> handle(IpolProxyException ex) {
		Envisu4ServiceError ipolServiceError = ex.getIpolServiceError();
		HttpStatus status;
		if (ipolServiceError.getErrorCode().equals(ServiceErrorCode.IPOL_RESOURCE_NOT_FOUND))
			status = HttpStatus.NOT_FOUND;
		else
			status = HttpStatus.INTERNAL_SERVER_ERROR;

		return new ResponseEntity<Envisu4ServiceError>(ipolServiceError, status);
	}
}
