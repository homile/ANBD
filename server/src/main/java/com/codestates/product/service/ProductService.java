package com.codestates.product.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.codestates.exception.CustomException;
import com.codestates.member.entity.Member;
import com.codestates.member.service.MemberService;
import com.codestates.pimage.entity.Pimage;
import com.codestates.pimage.repository.PimageRepository;
import com.codestates.product.entity.Product;
import com.codestates.product.mapper.ProductMapper;
import com.codestates.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class ProductService{

    private final MemberService memberService;
    private final ProductRepository productRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;
    private final PimageRepository pimageRepository ;
    private final ProductMapper mapper;


    /**
     * 제품 등록
     */
    public Product createProduct(Product product, Long memberId, List<Pimage> pimageList) {

        System.out.println("product.getPcategory()" + product.getPcategory());
        pimageList.stream().forEach(pimage -> {
            pimage.setProduct(product);
        });
        Member member = memberService.findVerifiedMember(memberId);
        product.setMember(member);
        return productRepository.save(product);
    }

    /**
     * 제품 수정
     */
    public Product updateProduct(Product product, Long memberId) {

        Product certifiedProduct = verifyProduct(product.getProductId());
        memberService.findVerifiedMember(memberId);
        verifyMemberProduct(memberId, certifiedProduct);

        Optional.ofNullable(product.getTitle()).ifPresent(certifiedProduct::setTitle);
        Optional.ofNullable(product.getDescription()).ifPresent(certifiedProduct::setDescription);
        Optional.ofNullable(product.getProductStatus()).ifPresent(certifiedProduct::setProductStatus);
        Optional.ofNullable(product.getPcategory()).ifPresent(certifiedProduct::setPcategory);
        certifiedProduct.setLastEditDate(LocalDateTime.now());
        return productRepository.save(certifiedProduct);
    }

    // 제품 등록 여부 확인
    public Product verifyProduct(Long productId) {
        return productRepository.findById(productId).orElseThrow(() -> new CustomException("Product not Found", HttpStatus.NOT_FOUND));
    }

    // 제품 등록한 멤버가 맞는지 확인
    private void verifyMemberProduct(Long memberId, Product certifiedProduct) {
        memberService.findVerifiedMember(memberId);
        if (!certifiedProduct.getMember().getMemberId().equals(memberId)) {
            throw new CustomException("You are not the member of this product", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * AWS 이미지 등록
     */
    public List<Pimage> uploadImage(List<MultipartFile> multipartFileList, List<String> imageUrlList) {


        List<Pimage> pimageList = multipartFileList.stream().map(file -> {
            Pimage pimage = new Pimage();
            String fileName = createFileName(file.getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            System.out.println("fileName :" + fileName);
            System.out.println(bucket);

            String url = generateUrl(fileName, HttpMethod.GET);
            pimage.setImageUrl(url);
            imageUrlList.add(url);

            try(InputStream inputStream = file.getInputStream()) {
                amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch(IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.");
            }
            return pimage;
        }).collect(Collectors.toList());

        return pimageList;
    }

    //    List<String> fileUrlList = new ArrayList<>();
//
//        multipartFileList.forEach(file -> {
//            String fileName = createFileName(file.getOriginalFilename());
//            ObjectMetadata objectMetadata = new ObjectMetadata();
//            objectMetadata.setContentLength(file.getSize());
//            objectMetadata.setContentType(file.getContentType());
//            System.out.println("fileName :" + fileName);
//            System.out.println(bucket);
//            String url = generateUrl(fileName, HttpMethod.GET);
//            fileUrlList.add(url);
//            settingPimage(url, product);
//
//            try(InputStream inputStream = file.getInputStream()) {
//                amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
//                        .withCannedAcl(CannedAccessControlList.PublicRead));
//            } catch(IOException e) {
//                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.");
//            }
//        });

//    private void settingPimage(String url, Product product) {
//        Pimage pimage = new Pimage();
//        pimage.setProduct(product);
//    }

    /**
     * 이미지 업데이트
     */
    public List<String> updateImage(long productId, List<String> updatedImageUrl) {

//        updatedImageUrl.stream().forEach(image -> System.out.println("image0 : " + image)); // 남길거 이미지 1개 request

        Optional<List<Pimage>> optionalPimageList = pimageRepository.findByProductId(productId);
        List<Pimage> legacyPimageList = optionalPimageList.orElseThrow(() -> new CustomException("Image not found", HttpStatus.NOT_FOUND));

//        legacyPimageList.stream().forEach(image -> System.out.println("image1 : " + image.getImageUrl())); // 기존 3개


        List<Pimage> deleteImageList = legacyPimageList.stream()
                .filter(image -> !updatedImageUrl.contains(image.getImageUrl()))
                .collect(Collectors.toList());


//        deleteImageList.stream().forEach(image -> System.out.println("image21 : " + image.getImageUrl())); // 지울거 URL
//        deleteImageList.stream().forEach(image -> System.out.println("image22 : " + image.getPimageId())); // 지울거 ID

        deleteImageList.stream()
                .forEach(deleteimage -> pimageRepository.deleteById(deleteimage.getPimageId()));

        ////////////////여기서 쿼리 안날라간다....

        List<Pimage> modifiedPimageList = pimageRepository.findByProductId(productId).get();

//        modifiedPimageList.stream().forEach(image -> System.out.println("image3 : " + image.getImageUrl())); // 응답줄거 이미지

        List<String> modifiedImageUrlList = modifiedPimageList.stream()
                .map(image -> image.getImageUrl())
                .collect(Collectors.toList());

//        modifiedImageUrlList.stream().forEach(image -> System.out.println("image4 : " + image)); // 응답줄거 URL

        return modifiedImageUrlList;
    }

    /**
     * AWS URL 생성
     */
    private String generateUrl(String fileName, HttpMethod httpMethod) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.MINUTE, 2); // Generated URL will be valid for 2minutes
        return amazonS3.generatePresignedUrl(bucket, fileName, calendar.getTime(), httpMethod).toString();
    }

    /**
     * AWS 파일 이름 생성
     */
    public String createFileName(String fileName) {
        return UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일(" + fileName + ") 입니다.");
        }
    }

    /**
     * 제품 삭제
     */
    public void deleteQuestion(long productId,long memberId) {
        Product certifiedProduct  = verifyProduct(productId);
        verifyMemberProduct(memberId,certifiedProduct);
        productRepository.delete(certifiedProduct);
    }


    /**
     * 제품 상세 조회
     */
    public Product findProduct(long productId) {

        Optional<Product> optionalProduct = productRepository.findById(productId);
        Product product = optionalProduct.orElseThrow(() -> new CustomException("Product not Found", HttpStatus.NOT_FOUND));

        System.out.println(product.getProductId());
        return product;
    }

//    /**
//     * 제품 리스트 조회
//     */
//    public Page<Product> findProductList(int page, int size) {
//        return productRepository.findAll(PageRequest.of(page, size,Sort.by("productId").descending()));
//    }

    /**
     * 제품 리스트 조회
     */
    public PageImpl<Product> findProductList(int page, int size, String pcategoryName, Product.ProductStatus status, String keyword) {
        PageRequest pageRequest = PageRequest.of(page, size);
        PageImpl<Product> productList = productRepository.findByCategoryStatusKeyword(pcategoryName, status, keyword,pageRequest);

        productList.stream().forEach(List -> System.out.println("List.getProductId(: " + List.getProductId()));
        return productList;
    }



    /**
     * 유저가 작성한 게시물 조회
     */
    public Page<Product> findMemberList(int page, int size,long memberId) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Optional<Page<Product>> optionalProductList = productRepository.findByMemberId(memberId,pageRequest);
        Page<Product> productList = optionalProductList.orElseThrow(() -> new CustomException("Member doesn't write Product", HttpStatus.NOT_FOUND));

        return productList;
    }
}