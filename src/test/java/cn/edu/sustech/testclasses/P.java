package cn.edu.sustech.testclasses;

import cn.edu.sustech.Inject;

public class P {

    @Inject
    private Q qDep;

    public Q getQDep() {
        return this.qDep;
    }
}
