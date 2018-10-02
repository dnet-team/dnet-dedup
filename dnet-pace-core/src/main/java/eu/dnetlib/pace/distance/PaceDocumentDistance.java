package eu.dnetlib.pace.distance;

import eu.dnetlib.pace.model.Document;

public class PaceDocumentDistance extends AbstractDistance<Document> {

	@Override
	protected Document toDocument(Document a) {
		return a;
	}

}
