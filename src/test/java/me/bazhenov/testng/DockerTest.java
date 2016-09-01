package me.bazhenov.testng;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DockerTest {

	private final Pattern unixDatePattern = Pattern.compile("[A-z]{3}\\s+[A-z]{3}\\s+[0-9]{1,2}\\s+" +
		"[0-9]{2}:[0-9]{2}:[0-9]{2}\\s+[A-Z]{3,4}\\s+[0-9]{4}");
	private Docker docker;

	@BeforeMethod
	public void setUp() {
		docker = new Docker();
	}

	@AfterMethod
	public void tearDown() throws IOException {
		docker.close();
	}

	@Test
	public void runSimpleContainer() throws IOException, InterruptedException, ParseException {
		ContainerExecution execution = new ContainerExecution("alpine", "date");

		String output = docker.executeAndReturnOutput(execution).trim();
		assertThat(unixDatePattern.matcher(output).matches(), is(true));
	}

	@Test
	public void runWithEnvVariables() throws IOException, InterruptedException, ParseException {
		ContainerExecution execution = new ContainerExecution("alpine", "sh", "-c", "echo $VAR");
		execution.addEnvironment("VAR", "value");

		String output = docker.executeAndReturnOutput(execution).trim();
		assertThat(output, equalTo("value"));
	}

	@Test
	public void executeDaemonizedContainer() throws IOException, InterruptedException {
		ContainerExecution execution = new ContainerExecution("alpine", "nc", "-l", "1234");
		execution.setExposePorts(singletonList(1234));

		String containerName = docker.start(execution);
		Map<Integer, Integer> ports = docker.getPublishedTcpPorts(containerName);
		assertThat(ports, hasKey(1234));
	}

	@Test
	public void parseJson() throws IOException {
		String json = Docker.readFully(getClass().getResourceAsStream("/inspect-example.json"));
		ObjectMapper jsonReader = new ObjectMapper();
		JsonNode root = jsonReader.readTree(json);
		Map<Integer, Integer> ports = Docker.doGetPublishedPorts(root);
		assertThat(ports, hasEntry(8888, 1500));
	}
}