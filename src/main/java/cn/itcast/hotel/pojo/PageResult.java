package cn.itcast.hotel.pojo;

import lombok.Data;

import java.util.List;

/**
 * Description ==> TODO
 * BelongsProject ==> hotel-demo
 * BelongsPackage ==> cn.itcast.hotel.pojo
 * Version ==> 1.0
 * CreateTime ==> 2022-11-09 16:17:33
 * Author ==> _02雪乃赤瞳楪祈校条祭_艾米丽可锦木千束木更七草荠_制作委员会_start
 */
@Data
public class PageResult {

    private  Long total;

    private List<HotelDoc> hotels;

}
