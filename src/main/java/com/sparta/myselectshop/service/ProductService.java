package com.sparta.myselectshop.service;

import com.sparta.myselectshop.dto.ProductMypriceRequestDto;
import com.sparta.myselectshop.dto.ProductRequestDto;
import com.sparta.myselectshop.dto.ProductResponseDto;
import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.entity.User;
import com.sparta.myselectshop.entity.UserRoleEnum;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;


    //myprice가 100원 이상이어야 함 (조건 추가)
    public static final int MIN_MY_PRICE = 100;

    public ProductResponseDto createProduct(ProductRequestDto requestDto, User user) {
        //받아온 Dto를 Entity 객체로 만들어줘야 함
        Product product = productRepository.save(new Product(requestDto,user));
        //new Product(requestDto) : requestDto 기반으로 만든 Product
        //.save() 로 저장 및 저장된 Product Entity가 반환됨

        return new ProductResponseDto(product); //반환타입에 맞춰
    }

    @Transactional //이걸 걸어야 변경을 탐지해서 update 함
    public ProductResponseDto updateProduct(Long id, ProductMypriceRequestDto requestDto) {
        int myprice = requestDto.getMyprice();
        //myprice가 100원 이상이어야 함

        if (myprice < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소" + MIN_MY_PRICE + "원 이상으로 설정해 주세요.");
        }

        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 상품을 찾을 수 없습니다.")
        );

        product.update(requestDto); //Product 객체의 상태를 ProductMypriceRequestDto의 데이터로 갱신하는 역할

        return new ProductResponseDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProducts(User user, int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page,size,sort);


        UserRoleEnum userRoleEnum = user.getRole();
        Page<Product> productList;
        if(userRoleEnum == UserRoleEnum.USER) {
            productList = productRepository.findAllByUser(user,pageable);
        }else{
            productList = productRepository.findAll(pageable);
        }


        /*//.findAll() = SELCT * FROM~
        List<ProductResponseDto> responseDtoList = new ArrayList<>();

        for (Product product : productList) {
            responseDtoList.add(new ProductResponseDto(product));
        }
*/
        return productList.map(ProductResponseDto::new);
    }

    //update는 @Transactional
    @Transactional
    public void updateBySearch(Long id, ItemDto itemDto) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new NullPointerException("해당 상품은 존재하지 않습니다.")
               );
        product.updateByItemDto(itemDto);
    }

    public List<ProductResponseDto> getAllProducts() {
        List<Product> productList = productRepository.findAll();
        //.findAll() = SELCT * FROM~
        List<ProductResponseDto> responseDtoList = new ArrayList<>();

        for (Product product : productList) {
            responseDtoList.add(new ProductResponseDto(product));
        }

        return responseDtoList;
    }
}
