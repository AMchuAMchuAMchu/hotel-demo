package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * Description ==> TODO
 * BelongsProject ==> hotel-demo
 * BelongsPackage ==> cn.itcast.hotel.pojo
 * Version ==> 1.0
 * CreateTime ==> 2022-11-09 16:15:04
 * Author ==> _02雪乃赤瞳楪祈校条祭_艾米丽可锦木千束木更七草荠_制作委员会_start
 */
@Data
public class RequestParams {

    private String key;

    private Integer page;

    private Integer size;

    private String sortBy;

    private String city;

    private String brand;

    private String starName;

    private Integer minPrice;

    private Integer maxPrice;

}
