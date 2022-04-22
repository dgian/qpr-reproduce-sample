package sample.resources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static java.util.Map.entry;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.SQS;

public class LocalStackResource implements QuarkusTestResourceLifecycleManager {

    private static final LocalStackContainer localstack =
        new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
            .withServices(SQS)
            .withReuse(true);

    @Override
    public Map<String, String> start() {
        localstack.start();

        return Map.ofEntries(
            entry("quarkus.sqs.aws.credentials.type", "static"),
            entry("quarkus.sqs.aws.credentials.static-provider.access-key-id", localstack.getAccessKey()),
            entry("quarkus.sqs.aws.credentials.static-provider.secret-access-key", localstack.getSecretKey()),
            entry("quarkus.sqs.aws.region", localstack.getRegion()),
            entry("quarkus.sqs.endpoint-override", hostUrl())
        );
    }

    @Override
    public void stop() {
        localstack.stop();
    }

    public static LocalStackContainer localstack() {
        return localstack;
    }

    public static String hostUrl() {
        return "http://%s:%d".formatted(localstack.getHost(), localstack.getMappedPort(4566));
    }

    public static String queueUrl() {
        return "%s/000000000000/".formatted(hostUrl());
    }
}
