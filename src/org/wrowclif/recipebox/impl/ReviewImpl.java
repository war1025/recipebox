package org.wrowclif.recipebox.impl;

import org.wrowclif.recipebox.Review;

public class ReviewImpl implements Review {

	private int rating;
	private long date;
	private String comments;

	public ReviewImpl() {

	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
