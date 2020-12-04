
package eu.dnetlib.maven.plugin.properties;

import static eu.dnetlib.maven.plugin.properties.GenerateOoziePropertiesMojo.PROPERTY_NAME_SANDBOX_NAME;
import static eu.dnetlib.maven.plugin.properties.GenerateOoziePropertiesMojo.PROPERTY_NAME_WF_SOURCE_DIR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.*;

/** @author mhorst, claudio.atzori */
public class GenerateOoziePropertiesMojoTest {

	private static final GenerateOoziePropertiesMojo mojo = new GenerateOoziePropertiesMojo();

	public void clearSystemProperties() {
		System.clearProperty(PROPERTY_NAME_SANDBOX_NAME);
		System.clearProperty(PROPERTY_NAME_WF_SOURCE_DIR);
	}

	@Test
	public void testExecuteEmpty() throws Exception {
		clearSystemProperties();

		// execute
		mojo.execute();

		// assert
		assertNull(System.getProperty(PROPERTY_NAME_SANDBOX_NAME));
	}

	@Test
	public void testExecuteSandboxNameAlreadySet() throws Exception {

		clearSystemProperties();
		// given
		String workflowSourceDir = "eu/dnetlib/dhp/wf/transformers";
		String sandboxName = "originalSandboxName";
		System.setProperty(PROPERTY_NAME_WF_SOURCE_DIR, workflowSourceDir);
		System.setProperty(PROPERTY_NAME_SANDBOX_NAME, sandboxName);

		// execute
		mojo.execute();

		// assert
		assertEquals(sandboxName, System.getProperty(PROPERTY_NAME_SANDBOX_NAME));
	}

	@Test  //fails
	public void testExecuteEmptyWorkflowSourceDir() throws Exception {
		clearSystemProperties();

		// given
		String workflowSourceDir = "";
		System.setProperty(PROPERTY_NAME_WF_SOURCE_DIR, workflowSourceDir);

		// execute
		mojo.execute();

		// assert
		assertNull(System.getProperty(PROPERTY_NAME_SANDBOX_NAME));
	}

	@Test
	public void testExecuteNullSandboxNameGenerated() throws Exception {
		clearSystemProperties();

		// given
		String workflowSourceDir = "eu/dnetlib/dhp/";
		System.setProperty(PROPERTY_NAME_WF_SOURCE_DIR, workflowSourceDir);

		// execute
		mojo.execute();

		// assert
		assertNull(System.getProperty(PROPERTY_NAME_SANDBOX_NAME));
	}

	@Test
	public void testExecute() throws Exception {

		clearSystemProperties();
		// given
		String workflowSourceDir = "eu/dnetlib/dhp/wf/transformers";
		System.setProperty(PROPERTY_NAME_WF_SOURCE_DIR, workflowSourceDir);

		// execute
		mojo.execute();

		// assert
		assertEquals("wf/transformers", System.getProperty(PROPERTY_NAME_SANDBOX_NAME));
	}

	@Test
	public void testExecuteWithoutRoot() throws Exception {

		clearSystemProperties();
		// given
		String workflowSourceDir = "wf/transformers";
		System.setProperty(PROPERTY_NAME_WF_SOURCE_DIR, workflowSourceDir);

		// execute
		mojo.execute();

		// assert
		assertEquals("wf/transformers", System.getProperty(PROPERTY_NAME_SANDBOX_NAME));
	}
}
