package com.afp.medialab.envisu4.tools.controller.models;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAnimatedGifResponse {

	private String gifURL;

	public CreateAnimatedGifResponse(String gifUrl) {
		this.gifURL = gifUrl;
	}

	public String getGifURL() {
		return gifURL;
	}

	public void setGifURL(String gifURL) {
		this.gifURL = gifURL;
	}

}
