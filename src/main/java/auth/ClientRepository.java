package auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByClientId(String clientId);
}
