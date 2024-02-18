package com.gamecity.scrabble.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.gamecity.scrabble.Constants;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A chat represents the messages sent by the {@link Player players} in a {@link Game game}
 * 
 * @author ekarakus
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Entity(name = "Chat")
@Table(name = "chats")
@NamedQueries({
        @NamedQuery(name = Constants.NamedQuery.getChats, query = "Select new Chat(c, u.username) from Chat c, User u"
                + " where c.gameId = :gameId and c.userId = u.id order by c.id asc")
})
public class Chat extends AbstractEntity {

    /**
     * Named query constructor
     * 
     * @param chat
     * @param username
     */
    public Chat(Chat chat, String username) {
        this.id = chat.id;
        this.createdDate = chat.createdDate;
        this.lastUpdatedDate = chat.lastUpdatedDate;
        this.gameId = chat.gameId;
        this.userId = chat.userId;
        this.username = username;
        this.message = chat.message;
    }

    // @JoinColumn(name = "game_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_CHAT_GAME"))
    @Column(name = "game_id", nullable = false)
    private Long gameId;

    // @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name =
    // "FK_CHAT_USER"))
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Transient
    private String username;

    @Column(name = "message", nullable = false)
    private String message;

}
