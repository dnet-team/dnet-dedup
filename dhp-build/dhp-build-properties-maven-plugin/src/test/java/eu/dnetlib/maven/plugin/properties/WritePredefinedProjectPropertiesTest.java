
package eu.dnetlib.maven.plugin.properties;

import static eu.dnetlib.maven.plugin.properties.WritePredefinedProjectProperties.PROPERTY_PREFIX_ENV;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

import java.io.*;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

/** @author mhorst, claudio.atzori */
@Disabled
@ExtendWith(MockitoExtension.class)
public class WritePredefinedProjectPropertiesTest {

	@Mock
	private MavenProject mavenProject;

	private WritePredefinedProjectProperties mojo;

	@TempDir File testFolder;

	public void init(File testFolder) {
		MockitoAnnotations.initMocks(this);
		mojo = new WritePredefinedProjectProperties();
		mojo.outputFile = getPropertiesFileLocation(testFolder);
		mojo.project = mavenProject;
		lenient().doReturn(new Properties()).when(mavenProject).getProperties();
	}

	// ----------------------------------- TESTS ---------------------------------------------

	@Test
	public void testExecuteEmpty() throws Exception {
		init(testFolder);
		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(mojo.outputFile.getParentFile());
		assertEquals(0, storedProperties.size());
	}

	@Test
	public void testExecuteWithProjectProperties() throws Exception {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		doReturn(projectProperties).when(mavenProject).getProperties();

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(mojo.outputFile.getParentFile());
		assertEquals(1, storedProperties.size());
		assertTrue(storedProperties.containsKey(key));
		assertEquals(value, storedProperties.getProperty(key));
	}

	@Test()
	public void testExecuteWithProjectPropertiesAndInvalidOutputFile(@TempDir File testFolder) {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		doReturn(projectProperties).when(mavenProject).getProperties();
		mojo.outputFile = testFolder;

		// execute
		Assertions.assertThrows(MojoExecutionException.class, () -> mojo.execute());
	}

	@Test
	public void testExecuteWithProjectPropertiesExclusion(@TempDir File testFolder) throws Exception {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		String excludedKey = "excludedPropertyKey";
		String excludedValue = "excludedPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		projectProperties.setProperty(excludedKey, excludedValue);
		doReturn(projectProperties).when(mavenProject).getProperties();
		mojo.setExclude(excludedKey);

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertEquals(1, storedProperties.size());
		assertTrue(storedProperties.containsKey(key));
		assertEquals(value, storedProperties.getProperty(key));
	}

	@Test
	public void testExecuteWithProjectPropertiesInclusion(@TempDir File testFolder) throws Exception {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		String includedKey = "includedPropertyKey";
		String includedValue = "includedPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		projectProperties.setProperty(includedKey, includedValue);
		doReturn(projectProperties).when(mavenProject).getProperties();
		mojo.setInclude(includedKey);

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertEquals(1, storedProperties.size());
		assertTrue(storedProperties.containsKey(includedKey));
		assertEquals(includedValue, storedProperties.getProperty(includedKey));
	}

	@Test
	public void testExecuteIncludingPropertyKeysFromFile(@TempDir File testFolder) throws Exception {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		String includedKey = "includedPropertyKey";
		String includedValue = "includedPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		projectProperties.setProperty(includedKey, includedValue);
		doReturn(projectProperties).when(mavenProject).getProperties();

		File includedPropertiesFile = new File(testFolder, "included.properties");
		Properties includedProperties = new Properties();
		includedProperties.setProperty(includedKey, "irrelevantValue");
		includedProperties.store(new FileWriter(includedPropertiesFile), null);

		mojo.setIncludePropertyKeysFromFiles(new String[] {
			includedPropertiesFile.getAbsolutePath()
		});

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertEquals(1, storedProperties.size());
		assertTrue(storedProperties.containsKey(includedKey));
		assertEquals(includedValue, storedProperties.getProperty(includedKey));
	}

	@Test
	public void testExecuteIncludingPropertyKeysFromClasspathResource(@TempDir File testFolder)
		throws Exception {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		String includedKey = "includedPropertyKey";
		String includedValue = "includedPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		projectProperties.setProperty(includedKey, includedValue);
		doReturn(projectProperties).when(mavenProject).getProperties();

		mojo
			.setIncludePropertyKeysFromFiles(
				new String[] {
					"/eu/dnetlib/maven/plugin/properties/included.properties"
				});

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertEquals(1, storedProperties.size());
		assertTrue(storedProperties.containsKey(includedKey));
		assertEquals(includedValue, storedProperties.getProperty(includedKey));
	}

	@Test
	public void testExecuteIncludingPropertyKeysFromBlankLocation() {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		String includedKey = "includedPropertyKey";
		String includedValue = "includedPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		projectProperties.setProperty(includedKey, includedValue);
		doReturn(projectProperties).when(mavenProject).getProperties();

		mojo.setIncludePropertyKeysFromFiles(new String[] {
			""
		});

		// execute
		Assertions.assertThrows(MojoExecutionException.class, () -> mojo.execute());
	}

	@Test
	public void testExecuteIncludingPropertyKeysFromXmlFile(@TempDir File testFolder)
		throws Exception {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		String includedKey = "includedPropertyKey";
		String includedValue = "includedPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		projectProperties.setProperty(includedKey, includedValue);
		doReturn(projectProperties).when(mavenProject).getProperties();

		File includedPropertiesFile = new File(testFolder, "included.xml");
		Properties includedProperties = new Properties();
		includedProperties.setProperty(includedKey, "irrelevantValue");
		includedProperties.storeToXML(new FileOutputStream(includedPropertiesFile), null);

		mojo.setIncludePropertyKeysFromFiles(new String[] {
			includedPropertiesFile.getAbsolutePath()
		});

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertEquals(1, storedProperties.size());
		assertTrue(storedProperties.containsKey(includedKey));
		assertEquals(includedValue, storedProperties.getProperty(includedKey));
	}

	@Test
	public void testExecuteIncludingPropertyKeysFromInvalidXmlFile(@TempDir File testFolder)
		throws Exception {
		init(testFolder);
		// given
		String key = "projectPropertyKey";
		String value = "projectPropertyValue";
		String includedKey = "includedPropertyKey";
		String includedValue = "includedPropertyValue";
		Properties projectProperties = new Properties();
		projectProperties.setProperty(key, value);
		projectProperties.setProperty(includedKey, includedValue);
		doReturn(projectProperties).when(mavenProject).getProperties();

		File includedPropertiesFile = new File(testFolder, "included.xml");
		Properties includedProperties = new Properties();
		includedProperties.setProperty(includedKey, "irrelevantValue");
		includedProperties.store(new FileOutputStream(includedPropertiesFile), null);

		mojo.setIncludePropertyKeysFromFiles(new String[] {
			includedPropertiesFile.getAbsolutePath()
		});

		// execute
		Assertions.assertThrows(MojoExecutionException.class, () -> mojo.execute());
	}

	@Test
	public void testExecuteWithQuietModeOn(@TempDir File testFolder) throws Exception {
		init(testFolder);
		// given
		mojo.setQuiet(true);
		mojo.setIncludePropertyKeysFromFiles(new String[] {
			"invalid location"
		});

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertEquals(0, storedProperties.size());
	}

	@Test
	public void testExecuteIncludingPropertyKeysFromInvalidFile() {
		init(testFolder);
		// given
		mojo.setIncludePropertyKeysFromFiles(new String[] {
			"invalid location"
		});

		// execute
		Assertions.assertThrows(MojoExecutionException.class, () -> mojo.execute());
	}

	@Test
	public void testExecuteWithEnvironmentProperties(@TempDir File testFolder) throws Exception {
		init(testFolder);
		// given
		mojo.setIncludeEnvironmentVariables(true);

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertTrue(storedProperties.size() > 0);
		for (Object currentKey : storedProperties.keySet()) {
			assertTrue(((String) currentKey).startsWith(PROPERTY_PREFIX_ENV));
		}
	}

	@Test
	public void testExecuteWithSystemProperties(@TempDir File testFolder) throws Exception {
		init(testFolder);
		// given
		String key = "systemPropertyKey";
		String value = "systemPropertyValue";
		System.setProperty(key, value);
		mojo.setIncludeSystemProperties(true);

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertTrue(storedProperties.size() > 0);
		assertTrue(storedProperties.containsKey(key));
		assertEquals(value, storedProperties.getProperty(key));
	}

	@Test
	public void testExecuteWithSystemPropertiesAndEscapeChars(@TempDir File testFolder)
		throws Exception {
		init(testFolder);
		// given
		String key = "systemPropertyKey ";
		String value = "systemPropertyValue";
		System.setProperty(key, value);
		mojo.setIncludeSystemProperties(true);
		String escapeChars = "cr,lf,tab,|";
		mojo.setEscapeChars(escapeChars);

		// execute
		mojo.execute();

		// assert
		assertTrue(mojo.outputFile.exists());
		Properties storedProperties = getStoredProperties(testFolder);
		assertTrue(storedProperties.size() > 0);
		assertFalse(storedProperties.containsKey(key));
		assertTrue(storedProperties.containsKey(key.trim()));
		assertEquals(value, storedProperties.getProperty(key.trim()));
	}

	// ----------------------------------- PRIVATE -------------------------------------------

	private File getPropertiesFileLocation(File testFolder) {
		return new File(testFolder, "test.properties");
	}

	private Properties getStoredProperties(File testFolder)
		throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(getPropertiesFileLocation(testFolder)));
		return properties;
	}
}
