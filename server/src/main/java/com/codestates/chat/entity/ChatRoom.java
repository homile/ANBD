package com.codestates.chat.entity;

import com.codestates.audit.Auditable;
import com.codestates.member.entity.Member;
import com.codestates.product.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoom extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatRoom_id")
    private Long id;

    @Column(nullable = false)
    private String roomId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Member seller;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private Member buyer;
}
