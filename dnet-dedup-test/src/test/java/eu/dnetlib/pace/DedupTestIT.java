package eu.dnetlib.pace;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.OozieClientException;
import org.apache.oozie.client.WorkflowJob;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static junit.framework.Assert.assertEquals;

public class DedupTestIT {

    @Test
    public void deduplicationTest() throws OozieClientException, InterruptedException {

        //read properties to use in the oozie workflow
        Properties prop = readProperties("/eu/dnetlib/test/properties/config.properties");

        /*OOZIE WORKFLOW CREATION AND LAUNCH*/
        // get a OozieClient for local Oozie
        OozieClient wc = new OozieClient("http://hadoop-edge3.garr-pa1.d4science.org:11000/oozie");

        // create a workflow job configuration and set the workflow application path
        Properties conf = wc.createConfiguration();
        conf.setProperty(OozieClient.APP_PATH, "hdfs://hadoop-rm1.garr-pa1.d4science.org:8020/user/michele.debonis/oozieJob/workflow.xml");
        conf.setProperty(OozieClient.USER_NAME, "michele.debonis");
        conf.setProperty("oozie.action.sharelib.for.spark", "spark2");
        conf.setProperty("oozie.use.system.libpath", "true");

        // setting workflow parameters
        conf.setProperty("jobTracker", "hadoop-rm3.garr-pa1.d4science.org:8032");
        conf.setProperty("nameNode", "hdfs://hadoop-rm1.garr-pa1.d4science.org:8020");
        conf.setProperty("dedupConfiguration", prop.getProperty("dedup.configuration"));
        conf.setProperty("inputSpace", prop.getProperty("input.space"));
        conf.setProperty("outputPath", prop.getProperty("output"));
        conf.setProperty("statisticsPath", prop.getProperty("dedup.statistics"));

        // submit and start the workflow job
        String jobId = wc.run(conf);
        System.out.println("Workflow job submitted");

        // wait until the workflow job finishes printing the status every 10 seconds
        while (wc.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
            System.out.println(wc.getJobInfo(jobId));;
            Thread.sleep(10 * 1000);
        }

        // print the final status of the workflow job
        System.out.println(wc.getJobInfo(jobId));
//        System.out.println("JOB LOG = " + wc.getJobLog(jobId));

        assertEquals(WorkflowJob.Status.SUCCEEDED, wc.getJobInfo(jobId).getStatus());

    }


    static Properties readProperties(final String propFile) {

        Properties prop = new Properties();
        try {
            prop.load(DedupTestIT.class.getResourceAsStream(propFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

}
