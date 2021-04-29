package com.afp.medialab.envisu4.tools.controller.models;

import java.util.List;

public class CreateAnimatedGifHistory {

	private List<AnimatedGif> animatedGifs;

	public CreateAnimatedGifHistory(List<AnimatedGif> animatedGifs) {
		this.animatedGifs = animatedGifs;
	}

	public List<AnimatedGif> getAnimatedGifs() {
		return animatedGifs;
	}

	public void setAnimatedGifs(List<AnimatedGif> animatedGifs) {
		this.animatedGifs = animatedGifs;
	}

}
