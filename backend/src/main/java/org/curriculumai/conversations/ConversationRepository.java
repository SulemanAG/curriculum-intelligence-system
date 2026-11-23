package org.curriculumai.conversations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {

    @Query("SELECT c FROM Conversation c WHERE c.user.id = :userId AND c.archived = false ORDER BY c.updatedAt DESC")
    List<Conversation> findAllActiveByUserId(@Param("userId") String userId);

    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.messages WHERE c.id = :id AND c.user.id = :userId")
    Optional<Conversation> findByIdAndUserIdWithMessages(@Param("id") String id, @Param("userId") String userId);
}

