package ebag.hd.bean;

import com.chad.library.adapter.base.entity.IExpandable;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.ArrayList;
import java.util.List;

import ebag.hd.adapter.UnitAdapter;

public class UnitBean implements
        IExpandable<UnitBean.ChapterBean>, MultiItemEntity {
    /**
     * id : 150
     * code : 1
     * name : 第一单元
     * bookVersionId : 362
     * pid : 0
     * unitCode : 1330
     * resultBookUnitOrCatalogVos : [{"id":2208,"code":"1","name":"窃读记","bookVersionId":"0","pid":"150","unitCode":"1338","resultBookUnitOrCatalogVos":[]},{"id":2209,"code":"2","name":"小苗与大树的对话","bookVersionId":"0","pid":"150","unitCode":"1339","resultBookUnitOrCatalogVos":[]},{"id":2210,"code":"3","name":"走遍天下书为侣","bookVersionId":"0","pid":"150","unitCode":"1340","resultBookUnitOrCatalogVos":[]},{"id":2211,"code":"4","name":"我的\u201c长生果\u201d","bookVersionId":"0","pid":"150","unitCode":"1341","resultBookUnitOrCatalogVos":[]}]
     */

    private int id;
    private String code;
    private String name;
    private String bookVersionId;
    private String pid;
    private String unitCode;
    private List<ChapterBean> resultBookUnitOrCatalogVos;
    private boolean isUnit = true;

    public boolean isUnit() {
        return isUnit;
    }

    public void setUnit(boolean unit) {
        isUnit = unit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBookVersionId() {
        return bookVersionId;
    }

    public void setBookVersionId(String bookVersionId) {
        this.bookVersionId = bookVersionId;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public List<ChapterBean> getResultBookUnitOrCatalogVos() {
        if(resultBookUnitOrCatalogVos == null || resultBookUnitOrCatalogVos.size() == 0){
            resultBookUnitOrCatalogVos = new ArrayList<>();
            ChapterBean chapterBean = new ChapterBean();
            chapterBean.setId(id);
            chapterBean.setBookVersionId(bookVersionId);
            chapterBean.setCode(code);
            chapterBean.setName(name);
            chapterBean.setPid(pid);
            chapterBean.setUnitCode(unitCode);
            resultBookUnitOrCatalogVos.add(chapterBean);
        }
        return resultBookUnitOrCatalogVos;
    }

    public void setResultBookUnitOrCatalogVos(List<ChapterBean> resultBookUnitOrCatalogVos) {
        this.resultBookUnitOrCatalogVos = resultBookUnitOrCatalogVos;
    }

    private boolean isExpand;
    @Override
    public boolean isExpanded() {
        return isExpand;
    }

    @Override
    public void setExpanded(boolean expanded) {
        isExpand = expanded;
    }

    @Override
    public List<ChapterBean> getSubItems() {
        return resultBookUnitOrCatalogVos;
    }

    @Override
    public int getLevel() {
        return UnitAdapter.LEVEL_ONE;
    }

    @Override
    public int getItemType() {
        return UnitAdapter.LEVEL_ONE;
    }

    public static class ChapterBean implements MultiItemEntity {
        /**
         * id : 2208
         * code : 1
         * name : 窃读记
         * bookVersionId : 0
         * pid : 150
         * unitCode : 1338
         * resultBookUnitOrCatalogVos : []
         */

        private int id;
        private String code;
        private String name;
        private String bookVersionId;
        private String pid;
        private String unitCode;
        private List<ChapterBean> resultBookUnitOrCatalogVos;
        private boolean isUnit;

        public boolean isUnit() {
            return isUnit;
        }

        public void setUnit(boolean unit) {
            isUnit = unit;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBookVersionId() {
            return bookVersionId;
        }

        public void setBookVersionId(String bookVersionId) {
            this.bookVersionId = bookVersionId;
        }

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public String getUnitCode() {
            return unitCode;
        }

        public void setUnitCode(String unitCode) {
            this.unitCode = unitCode;
        }

        public List<ChapterBean> getResultBookUnitOrCatalogVos() {
            return resultBookUnitOrCatalogVos;
        }

        public void setResultBookUnitOrCatalogVos(List<ChapterBean> resultBookUnitOrCatalogVos) {
            this.resultBookUnitOrCatalogVos = resultBookUnitOrCatalogVos;
        }

        @Override
        public int getItemType() {
            return UnitAdapter.LEVEL_TWO;
        }
    }
}