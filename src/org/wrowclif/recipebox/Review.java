package org.wrowclif.recipebox;

public interface Review {

	public long getId();

	public int getRating();

	public void setRating(int rating);

	public long getDate();

	public String getComments();

	public void setComments(String comments);

}
