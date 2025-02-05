package com.sparta.myselectshop.scheduler;

import com.sparta.myselectshop.entity.Product;
import com.sparta.myselectshop.naver.dto.ItemDto;
import com.sparta.myselectshop.naver.service.NaverApiService;
import com.sparta.myselectshop.repository.ProductRepository;
import com.sparta.myselectshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "Scheduler")
@Component
@RequiredArgsConstructor
public class Scheduler {


    private final NaverApiService naverApiService;//해당 목록을 재검색해야하므로
    private final ProductService productService;
    private final ProductRepository productRepository;

    // 초, 분, 시, 일, 월, 주 순서
   @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시
   // @Scheduled(cron = "*/10 * * * * *") // 테스트 용 (10초)
    public void updatePrice() throws InterruptedException {
        log.info("가격 업데이트 실행");
        //검색, 재검색해야할 product 목록 가져오기
        List<Product> productList = productRepository.findAll();

        //for문으로 가져온 product 가져오기
        for (Product product : productList) {

            // 1초에 한 상품 씩 조회합니다 (NAVER 제한)
            TimeUnit.SECONDS.sleep(1);

            // i 번째 관심 상품의 제목으로 검색을 실행합니다.
            String title = product.getTitle();
            List<ItemDto> itemDtoList = naverApiService.searchItems(title);

            if (itemDtoList.size() > 0) {
                ItemDto itemDto = itemDtoList.get(0); //0보다 크면 0번째 index에 있는 데이터를 가져옴 - 가장 일치하기 때문
                // i 번째 관심 상품 정보를 업데이트합니다.
                
                //해당 데이터의 id를 모두 가져옴
                Long id = product.getId();
                
                try {
                    productService.updateBySearch(id, itemDto);// id 및 정보들 넘겨줌 - 최신 가격으로 업데이트하는 메소드
                } catch (Exception e) {
                    log.error(id + " : " + e.getMessage());//오류가 나더라도 다른 상품들은 계속 조회될 수 있도록 try - catch
                }
            }
        }
    }

}