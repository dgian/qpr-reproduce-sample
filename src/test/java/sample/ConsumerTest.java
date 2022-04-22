package sample;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.Test;
import sample.resources.LocalStackResource;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(LocalStackResource.class)
class ConsumerTest {

    @Inject
    SqsClient sqsClient;

    @Inject
    Repository repository;

    @Test
    void test_consume() {
        repository.deleteAll().subscribe().withSubscriber(UniAssertSubscriber.create());

        final var payload = """
            {
                "firstName": "FirstName",
                "lastName": "LatName",
                "age": 18
            }
            """;
        
        SendMessageRequest message = SendMessageRequest.builder()
            .messageBody(payload)
            .queueUrl(LocalStackResource.queueUrl() + "/persons-queue")
            .build();

        // First 3 messages are processed fully
        sqsClient.sendMessage(message);
        sqsClient.sendMessage(message);
        sqsClient.sendMessage(message);


        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Uni<Long> count = repository.count();

        var sub = count.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem();

        assertEquals(3, sub.getItem());

        // --
        // 4th message is triggering the timeout error
        sqsClient.sendMessage(message);

        try {
            Thread.sleep(40000); // wait enough to trigger the timeout
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        UniAssertSubscriber<Long> sub2 = count.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem();

        assertEquals(4, sub2.getItem());

    }

    @Test
    void test_consumeUsingSession() {
        repository.deleteAll().subscribe().withSubscriber(UniAssertSubscriber.create());

        final var payload = """
            {
                "firstName": "FirstName",
                "lastName": "LatName",
                "age": 18
            }
            """;

        SendMessageRequest message = SendMessageRequest.builder()
            .messageBody(payload)
            .queueUrl(LocalStackResource.queueUrl() + "/persons-session-queue")
            .build();

        // First 3 messages are processed fully
        sqsClient.sendMessage(message);
        sqsClient.sendMessage(message);
        sqsClient.sendMessage(message);


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Uni<Long> count = repository.count();
        var sub = count.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem();

        assertEquals(3, sub.getItem());

        // --
        // 4th message is triggering the timeout error
        sqsClient.sendMessage(message);

        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        var sub2 = count.subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem();

        assertEquals(4, sub2.getItem());

    }
}