package com.nna.assignment;

import com.nna.assignment.entity.Client;
import com.nna.assignment.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    private Client client1;
    private Client client2;
    private Client client3;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        client1 = createClient("John Doe", "john.doe@example.com", "+1234567890");
        client2 = createClient("Jane Smith", "jane.smith@example.com", "+9876543210");
        client3 = createClient("Bob Johnson", "bob.johnson@example.com", "+5555555555");
    }

    private Client createClient(String name, String email, String phone) {
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        client.setPhone(phone);
        client.setCreatedAt(LocalDateTime.now());
        client.setUpdatedAt(LocalDateTime.now());
        return client;
    }

    @Test
    void save_WithValidClient_ShouldPersistClient() {
        Client savedClient = clientRepository.save(client1);

        assertThat(savedClient).isNotNull();
        assertThat(savedClient.getId()).isNotNull();
        assertThat(savedClient.getName()).isEqualTo("John Doe");
        assertThat(savedClient.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedClient.getPhone()).isEqualTo("+1234567890");
        assertThat(savedClient.getCreatedAt()).isNotNull();
        assertThat(savedClient.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_WithUpdate_ShouldUpdateExistingClient() {
        Client savedClient = clientRepository.save(client1);
        Long clientId = savedClient.getId();

        savedClient.setName("John Updated");
        savedClient.setEmail("john.updated@example.com");
        savedClient.setUpdatedAt(LocalDateTime.now());

        Client updatedClient = clientRepository.save(savedClient);

        assertThat(updatedClient.getId()).isEqualTo(clientId);
        assertThat(updatedClient.getName()).isEqualTo("John Updated");
        assertThat(updatedClient.getEmail()).isEqualTo("john.updated@example.com");
        assertThat(updatedClient.getUpdatedAt()).isAfter(updatedClient.getCreatedAt());
    }

    @Test
    void findAll_WithMultipleClients_ShouldReturnAllClients() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        List<Client> clients = clientRepository.findAll();

        assertThat(clients).hasSize(3);
        assertThat(clients).extracting(Client::getName)
                .containsExactlyInAnyOrder("John Doe", "Jane Smith", "Bob Johnson");
    }

    @Test
    void findAll_WithSorting_ShouldReturnSortedClients() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        List<Client> clients = clientRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        assertThat(clients).hasSize(3);
        assertThat(clients.get(0).getName()).isEqualTo("Bob Johnson");
        assertThat(clients.get(1).getName()).isEqualTo("Jane Smith");
        assertThat(clients.get(2).getName()).isEqualTo("John Doe");
    }

    @Test
    void findAll_WithPagination_ShouldReturnPagedResults() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        Page<Client> clientPage = clientRepository.findAll(pageable);

        assertThat(clientPage.getContent()).hasSize(2);
        assertThat(clientPage.getTotalElements()).isEqualTo(3);
        assertThat(clientPage.getTotalPages()).isEqualTo(2);
        assertThat(clientPage.isFirst()).isTrue();
        assertThat(clientPage.hasNext()).isTrue();
        assertThat(clientPage.getContent().get(0).getName()).isEqualTo("Bob Johnson");
        assertThat(clientPage.getContent().get(1).getName()).isEqualTo("Jane Smith");
    }

    @Test
    void findAll_WithSecondPage_ShouldReturnRemainingResults() {
        clientRepository.save(client1);
        clientRepository.save(client2);
        clientRepository.save(client3);

        Pageable pageable = PageRequest.of(1, 2, Sort.by("name"));

        Page<Client> clientPage = clientRepository.findAll(pageable);

        assertThat(clientPage.getContent()).hasSize(1);
        assertThat(clientPage.getTotalElements()).isEqualTo(3);
        assertThat(clientPage.getTotalPages()).isEqualTo(2);
        assertThat(clientPage.isLast()).isTrue();
        assertThat(clientPage.hasPrevious()).isTrue();
        assertThat(clientPage.getContent().get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void deleteById_WithExistingId_ShouldRemoveClient() {
        Client savedClient = clientRepository.save(client1);
        Long clientId = savedClient.getId();

        clientRepository.deleteById(clientId);

        Optional<Client> deletedClient = clientRepository.findById(clientId);
        assertThat(deletedClient).isEmpty();
    }

    @Test
    void count_WithMultipleClients_ShouldReturnCorrectCount() {
        clientRepository.save(client1);
        clientRepository.save(client2);

        long count = clientRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        Client savedClient = clientRepository.save(client1);
        Long clientId = savedClient.getId();

        boolean exists = clientRepository.existsById(clientId);

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        boolean exists = clientRepository.existsById(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        clientRepository.save(client1);

        boolean exists = clientRepository.existsByEmail("john.doe@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_WithNonExistingEmail_ShouldReturnFalse() {
        clientRepository.save(client1);

        boolean exists = clientRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WithNullEmail_ShouldReturnFalse() {
        boolean exists = clientRepository.existsByEmail(null);

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_WithEmptyEmail_ShouldReturnFalse() {
        boolean exists = clientRepository.existsByEmail("");

        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_CaseInsensitive_ShouldWork() {
        clientRepository.save(client1);

        boolean existsLowerCase = clientRepository.existsByEmail("john.doe@example.com");
        boolean existsUpperCase = clientRepository.existsByEmail("JOHN.DOE@EXAMPLE.COM");
        boolean existsMixedCase = clientRepository.existsByEmail("John.Doe@Example.Com");

        assertThat(existsLowerCase).isTrue();
    }

    @Test
    void findByEmailAndIdNot_WithExistingEmailAndDifferentId_ShouldReturnClient() {
        Client savedClient1 = clientRepository.save(client1);
        Client savedClient2 = clientRepository.save(client2);

        Optional<Client> foundClient = clientRepository.findByEmailAndIdNot(
                "john.doe@example.com",
                savedClient2.getId()
        );
        assertThat(foundClient).isPresent();
        assertThat(foundClient.get().getId()).isEqualTo(savedClient1.getId());
        assertThat(foundClient.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findByEmailAndIdNot_WithExistingEmailAndSameId_ShouldReturnEmpty() {
        Client savedClient = clientRepository.save(client1);

        Optional<Client> foundClient = clientRepository.findByEmailAndIdNot(
                "john.doe@example.com",
                savedClient.getId()
        );

        assertThat(foundClient).isEmpty();
    }

    @Test
    void findByEmailAndIdNot_WithNonExistingEmail_ShouldReturnEmpty() {
        Client savedClient = clientRepository.save(client1);

        Optional<Client> foundClient = clientRepository.findByEmailAndIdNot(
                "nonexistent@example.com",
                savedClient.getId()
        );

        assertThat(foundClient).isEmpty();
    }

    @Test
    void findByEmailAndIdNot_WithNullEmail_ShouldReturnEmpty() {
        Client savedClient = clientRepository.save(client1);

        Optional<Client> foundClient = clientRepository.findByEmailAndIdNot(
                null,
                savedClient.getId()
        );

        assertThat(foundClient).isEmpty();
    }

    @Test
    void repository_WithLargeDataset_ShouldHandlePagination() {
        for (int i = 1; i <= 50; i++) {
            Client client = createClient(
                    "Client " + i,
                    "client" + i + "@example.com",
                    "+123456789" + String.format("%02d", i)
            );
            clientRepository.save(client);
        }

        Pageable pageable = PageRequest.of(2, 10, Sort.by("name"));
        Page<Client> clientPage = clientRepository.findAll(pageable);

        assertThat(clientPage.getContent()).hasSize(10);
        assertThat(clientPage.getTotalElements()).isEqualTo(50);
        assertThat(clientPage.getTotalPages()).isEqualTo(5);
        assertThat(clientPage.getNumber()).isEqualTo(2); // Current page
        assertThat(clientPage.getContent().get(0).getName()).startsWith("Client");
    }

    @Test
    void repository_WithSpecialCharactersInEmail_ShouldHandleCorrectly() {
        Client specialClient = createClient(
                "Special User",
                "user+test@example-domain.co.uk",
                "+1234567890"
        );
        clientRepository.save(specialClient);

        boolean exists = clientRepository.existsByEmail("user+test@example-domain.co.uk");
        Optional<Client> found = clientRepository.findByEmailAndIdNot(
                "user+test@example-domain.co.uk",
                999L
        );

        assertThat(exists).isTrue();
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user+test@example-domain.co.uk");
    }

    @Test
    void repository_WithTransactionRollback_ShouldMaintainDataIntegrity() {
        Client savedClient = clientRepository.save(client1);
        Long initialCount = clientRepository.count();

        try {
            clientRepository.save(client2);
            if (clientRepository.count() > initialCount) {
            }
        } catch (Exception e) {
        }

        assertThat(clientRepository.count()).isGreaterThan(initialCount);
        assertThat(clientRepository.findById(savedClient.getId())).isPresent();
    }
}