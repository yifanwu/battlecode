package multiBot;

public class vector {
	public point start;
	public point end;
	public double normsq;
	
	public vector(int sx, int sy, int ex, int ey) {
		start = new point(sx, sy);
		end = new point(ex, ey);
		normsq = (ex - sx)*(ex - sx) + (ey - sy)*(ey - sy); 
	}
	
}
