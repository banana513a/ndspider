package com.ndcf.spider.crawler.bo;

public class Node{

    public String url;
    public String fileName;

    public Node(String url, String fileName) {
        this.url = url;
        this.fileName = fileName;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        sb.append(fileName);
//        sb.append(status);
        char[] charArr = sb.toString().toCharArray();
        int hash = 0;
        for (char c : charArr) {
            hash = hash * 131 + c;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Node) {
            if (((Node) obj).url.equals(this.url)
                    && ((Node) obj).fileName.equals(this.fileName)
//                    && ((Node) obj).status.equals(this.status)

                    ) {
                return true;
            }
        }

        return false;
    }

}
