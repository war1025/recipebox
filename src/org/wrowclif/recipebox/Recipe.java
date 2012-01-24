package recipebox;

import java.util.List;
import java.util.Set;

public interface Recipe {

	public Set<Ingredient> getIngredients();

	public List<Instruction> getSteps();

	public Set<Review> getReviews();

	public void addReview(Review r);

	public Set<Category> getCategories();
}
