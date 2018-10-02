package eu.dnetlib.pace.model.gt;

public class Match extends Author {

	private double score;

	public Match() {
		super();
	}

	public static Match from(final Author a) {
		final Match m = new Match();
		if (a.isWellFormed()) {
			m.setFirstname(a.getFirstname());
			m.setSecondnames(a.getSecondnames());
		}
		m.setFullname(a.getFullname());
		m.setId(a.getId());

		return m;
	}

	public double getScore() {
		return score;
	}

	public void setScore(final double score) {
		this.score = score;
	}

}
