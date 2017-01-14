package cn.bdqn.mybatis.plugin;


/**
 * 分页参数
 * @author 学术部
 * 封装分页参数。在执行分页查询时，向语句传递页号(pageIndex)和每页行数(pageSize),查询完毕后，将被回填总行数(rowCount)和总页数(pageCount)
 * */
public class PageParam {
	
	/**默认页号*/
	public static final int DEFAULT_PAGE_INDEX = 1;
	/**默认每页行数*/
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    /**
     * 使用默认页号(1)和默认每页行数(10)构建PageParam对象
     * */
    public PageParam() {
		this(DEFAULT_PAGE_INDEX,DEFAULT_PAGE_SIZE);
	}
    
    /**
     * 使用指定的页号和每页行数构建PageParam对象
     * @param pageIndex 页号
     * @param pageSize 每页行数
     * */
    public PageParam(int pageIndex, int pageSize) {
		super();
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}

    /**每页行数*/
	private int pageIndex;
	/**每页行数*/
    private int pageSize;
    /**总行数*/
    private int rowCount;
    /**总页数*/
    private int pageCount;
 
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getRowCount() {
		return rowCount;
	}
	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}
	public int getPageCount() {
		return pageCount;
	}
	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

    
    

}
