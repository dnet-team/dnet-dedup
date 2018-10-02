package eu.dnetlib.pace.distance;

import eu.dnetlib.pace.model.Field;

/**
 * Each field is configured with a distance algo which knows how to compute the distance (0-1) between the fields of two
 * objects.
 */
public interface DistanceAlgo {

	public abstract double distance(Field a, Field b);

	public double getWeight();

}
