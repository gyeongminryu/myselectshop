package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    public ProductResponseDto createProduct(ProductRequestDto requestDto) {
        //받아온 Dto를 Entity 객체로 만들어줘야 함
        Product product = productRepository.save(new Product(requestDto));
                            //new Product(requestDto) : requestDto 기반으로 만든 Product
                            //.save() 로 저장 및 저장된 Product Entity가 반환됨
        
        return new ProductResponseDto(product); //반환타입에 맞춰
    }
}
