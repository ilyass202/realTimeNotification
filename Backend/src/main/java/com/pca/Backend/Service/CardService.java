package com.pca.Backend.Service;

import org.springframework.stereotype.Service;

import com.pca.Backend.DTO.CardDtoRequest;
import com.pca.Backend.DTO.CardResponse;
import com.pca.Backend.Entity.Card;
import com.pca.Backend.Entity.StatusCard;
import com.pca.Backend.Repo.CardRepo;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepo repo;
    public CardResponse createCard(CardDtoRequest request){
        Card card = Card.builder().minAmount(request.minAmount())
        .cardType(request.cardType()).userId(request.userId())
        .statusCard(request.minAmount() > 0 ? StatusCard.ACTIVATED : StatusCard.CREATED).dateCreation(LocalDateTime.now()).build();
        repo.save(card);
        return new CardResponse("carte crée avec succés", card.getDateCreation(), card.getId());
    }

}
