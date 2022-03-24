package com.dl.playfun.utils;

import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.dl.playfun.R;
import com.dl.playfun.app.Injection;
import com.dl.playfun.entity.ConfigItemEntity;
import com.dl.playfun.entity.OccupationConfigItemEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wulei
 */
public class SystemDictUtils {

    public static String getCityByIds(List<Integer> ids) {
        if (ids == null) {
            return "";
        }
        List<ConfigItemEntity> data = Injection.provideDemoRepository().readCityConfig();
        StringBuffer sb = new StringBuffer();
        if (data != null && !data.isEmpty()) {
            for (ConfigItemEntity config : data) {
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i).intValue() == config.getId()) {
                        if (i == ids.size() - 1) {
                            sb.append(config.getName());
                        } else {
                            sb.append(config.getName()).append("/");
                        }
                    }

                }
            }
        }
        return sb.toString();
    }

    public static String getCityById(Integer id) {
        List<ConfigItemEntity> data = Injection.provideDemoRepository().readCityConfig();
        StringBuffer sb = new StringBuffer();
        if (id == null) {
            sb.append(StringUtils.getString(R.string.unknown));
        } else {
            if (data != null && !data.isEmpty()) {
                for (ConfigItemEntity config : data) {
                    if (id.intValue() == config.getId()) {
                        sb.append(config.getName());
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String getHopeObjectByIds(Integer id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        return getHopeObjectByIds(ids);
    }

    public static String getHopeObjectByIds(List<Integer> ids) {
        if (ids == null) {
            return "";
        }
        List<ConfigItemEntity> data = Injection.provideDemoRepository().readHopeObjectConfig();
        StringBuffer sb = new StringBuffer();
        if (data != null && !data.isEmpty()) {
            for (ConfigItemEntity config : data) {
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i) == null || config.getId() == null) {
                        continue;
                    }
                    if (ids.get(i).intValue() == config.getId().intValue()) {
                        sb.append(config.getName()).append("/");
                    }

                }
            }
        }
        String str = sb.toString();
        if (str.indexOf("/") != -1) {
            return str.substring(0, str.length() - 1);
        }
        return sb.toString();
    }

    public static String getProgramByIds(Integer id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        return getProgramByIds(ids);
    }

    //根据主题ID查询主题名
    public static String getProgramThemeById(Integer id) {
        if (ObjectUtils.isEmpty(id)) {
            return StringUtils.getString(R.string.fragment_issuance_program_title);
        }
        List<ConfigItemEntity> data = Injection.provideDemoRepository().readThemeConfig();
        String str = null;
        if (data != null && !data.isEmpty()) {
            for (ConfigItemEntity config : data) {
                if (config.getThemeId() != null && id.intValue() == config.getThemeId().intValue()) {
                    str = config.getName();
                }
            }
        }
        if (ObjectUtils.isEmpty(str)) {
            return StringUtils.getString(R.string.fragment_issuance_program_title);
        }
        return str;
    }

    public static String getProgramByIds(List<Integer> ids) {
        if (ids == null) {
            return "";
        }
        List<ConfigItemEntity> data = Injection.provideDemoRepository().readThemeConfig();
        StringBuffer sb = new StringBuffer();
        if (data != null && !data.isEmpty()) {
            for (ConfigItemEntity config : data) {
                for (int i = 0; i < ids.size(); i++) {
                    if (config.getThemeId() != null && ids.get(i).intValue() == config.getThemeId().intValue()) {
                        if (i == ids.size() - 1) {
                            sb.append(config.getName());
                        } else {
                            sb.append(config.getName()).append("/");
                        }
                    }

                }
            }
        }
        return sb.toString();
    }


    public static String getProgramTimeById(Integer id) {
        if (id == null) {
            return "";
        }
        List<ConfigItemEntity> data = Injection.provideDemoRepository().readProgramTimeConfig();
        StringBuffer sb = new StringBuffer();
        for (ConfigItemEntity c : data) {
            if (c.getId() == id) {
                sb.append(c.getName());
            }
        }
        return sb.toString();
    }

    public static String getWeightById(Integer id) {
        if (id == null) {
            return "";
        }
        List<ConfigItemEntity> data = Injection.provideDemoRepository().readWeightConfig();
        StringBuffer sb = new StringBuffer();
        for (ConfigItemEntity c : data) {
            if (c.getId().intValue() == id.intValue()) {
                sb.append(c.getName());
            }
        }
        return sb.toString();
    }

    public static String getHeightById(Integer id) {
        if (id == null) {
            return "";
        }
        List<ConfigItemEntity> data = Injection.provideDemoRepository().readHeightConfig();
        StringBuffer sb = new StringBuffer();
        for (ConfigItemEntity c : data) {
            if (c.getId().intValue() == id.intValue()) {
                sb.append(c.getName());
            }
        }
        return sb.toString();
    }

//    /**
//     * 根据工作ID查询工作名称
//     *
//     * @param id 工作ID
//     * @return
//     */
//    public static String getOccupationById(int id) {
//        List<OccupationConfigItemEntity> list = Injection.provideDemoRepository().readOccupationConfig();
//        String name = StringUtils.getString(R.string.unknown);
//        for (OccupationConfigItemEntity itemEntity : list) {
//            if (itemEntity.getId() == id) {
//                name = itemEntity.getName();
//                break;
//            }
//        }
//        return name;
//    }

    /**
     * 根据工作ID查询工作名称
     *
     * @param id 工作ID
     * @return
     */
    public static String getOccupationById(int id) {
        List<OccupationConfigItemEntity> list = Injection.provideDemoRepository().readOccupationConfig();
        String name = StringUtils.getString(R.string.unknown);
        for (OccupationConfigItemEntity occupationConfigItemEntity : list) {
            boolean find = false;
            for (OccupationConfigItemEntity.ItemEntity itemEntity : occupationConfigItemEntity.getItem()) {
                if (itemEntity.getId() == id) {
                    find = true;
                    name = itemEntity.getName();
                    break;
                }
            }
            if (find) {
                break;
            }
        }
        return name;
    }

    /**
     * 根据身高配置ID查询身高
     *
     * @param id 身高ID
     * @return
     */
    public static String getHeightById(int id) {
        List<ConfigItemEntity> list = Injection.provideDemoRepository().readHeightConfig();
        String name = StringUtils.getString(R.string.unknown);
        for (ConfigItemEntity configItemEntity : list) {
            if (configItemEntity.getId().intValue() == id) {
                name = configItemEntity.getName();
                break;
            }
        }
        return name;
    }

    /**
     * 根据体重配置ID查询体重
     *
     * @param id 体重ID
     * @return
     */
    public static String getWidthById(int id) {
        List<ConfigItemEntity> list = Injection.provideDemoRepository().readWeightConfig();
        String name = StringUtils.getString(R.string.unknown);
        for (ConfigItemEntity configItemEntity : list) {
            if (configItemEntity.getId().intValue() == id) {
                name = configItemEntity.getName();
                break;
            }
        }
        return name;
    }

    /**
     * 根据节目时间ID查询体重
     *
     * @param id 体重ID
     * @return
     */
    public static String getTimeById(int id) {
        List<ConfigItemEntity> list = Injection.provideDemoRepository().readProgramTimeConfig();
        String name = StringUtils.getString(R.string.unknown);
        for (ConfigItemEntity configItemEntity : list) {
            if (configItemEntity.getId().intValue() == id) {
                name = configItemEntity.getName();
                break;
            }
        }
        return name;
    }
}
