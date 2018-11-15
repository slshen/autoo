package autoo.util;

public interface IRandom {
	public long getInitialSeed();
	public int nextInt();
	public int nextInt(int n);
	public long nextLong();
	public boolean nextBoolean();
	public double nextDouble();
	public float nextFloat();
	public double nextGaussian(double sigma, double mu);
}
