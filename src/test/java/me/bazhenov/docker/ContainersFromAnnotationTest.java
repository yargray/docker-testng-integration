package me.bazhenov.docker;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static me.bazhenov.docker.Utils.readLineFrom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@ContainersFrom(SharedContainers.class)
@Listeners(DockerTestNgListener.class)
public class ContainersFromAnnotationTest {

	protected int hostPort;

	@AfterContainerStart
	public void setUpDocker(@ContainerPort(name = "nc1", port = 1234) int hostPort1) {
		this.hostPort = hostPort1;
	}

	@Test
	public void foo() throws IOException {
		assertThat(hostPort, greaterThan(1024));
		assertThat(readLineFrom(hostPort), equalTo("Hello"));
	}
}

@Container(name = "nc1", image = "alpine", publish = @Port(1234),
	command = {"nc", "-lkp", "1234", "-s", "0.0.0.0", "-e", "echo", "-e", "HTTP/1.1 200 OK\n\nHello"}
)
class SharedContainers {

}
