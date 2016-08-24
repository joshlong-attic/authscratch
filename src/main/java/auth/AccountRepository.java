package auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUsername(String username);
}
