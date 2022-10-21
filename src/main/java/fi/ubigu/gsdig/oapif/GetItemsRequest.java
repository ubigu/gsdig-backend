package fi.ubigu.gsdig.oapif;

public class GetItemsRequest {

    private final String collectionId;
    private final double[] bbox;
    private final int bboxSrid;
    private final int offset;
    private final int limit;
    private final int srid;

    public GetItemsRequest(String collectionId, double[] bbox, int bboxSrid, int offset, int limit, int srid) {
        this.collectionId = collectionId;
        this.bbox = bbox;
        this.bboxSrid = bboxSrid;
        this.offset = offset;
        this.limit = limit;
        this.srid = srid;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public double[] getBbox() {
        return bbox;
    }

    public int getBboxSrid() {
        return bboxSrid;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public int getSrid() {
        return srid;
    }

}
