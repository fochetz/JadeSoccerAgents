

public class Position {
	private final int max_x = 550;
	private final int max_y = 350;
	private float x;
	private float y;
	public Position(float x, float y) {
		super();
		this.x = x;
		this.y = y;
	}
	public float getX() {
		return x;
	}
	public void setX(float f) {
		if (f > max_x) this.x =max_x;
		else this.x = f;
	}
	public float getY() {
		return y;
	}
	public void setY(float g) {
		if(g > max_y) this.y = max_y;
		else this.y = g;
	}
	public float calculate_distance(Position second)
	{
		float distance = (float) (Math.sqrt(Math.pow((this.getX() - second.getX()),2) + Math.pow((this.getY() - second.getY()),2)));
		return distance;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + max_x;
		result = prime * result + max_y;
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (max_x != other.max_x)
			return false;
		if (max_y != other.max_y)
			return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x))
			return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y))
			return false;
		return true;
	}
	
		
}
