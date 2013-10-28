package edu.dartmouth.cs.myruns5;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.classifiers.Classifier;

class WekaClassifier {

  public static double classify(Object[] i)
    throws Exception {

    double p = Double.NaN;
    p = WekaClassifier.N6390c7a0(i);
    return p;
  }
  static double N6390c7a0(Object []i) {
    double p = Double.NaN;
    if (i[64] == null) {
      p = 1;
    } else if (((Double) i[64]).doubleValue() <= 7.216812) {
    p = WekaClassifier.Nb6220c71(i);
    } else if (((Double) i[64]).doubleValue() > 7.216812) {
    p = WekaClassifier.Nb5d5daa20(i);
    } 
    return p;
  }
  static double Nb6220c71(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 0;
    } else if (((Double) i[0]).doubleValue() <= 73.838152) {
    p = WekaClassifier.N5755eea72(i);
    } else if (((Double) i[0]).doubleValue() > 73.838152) {
    p = WekaClassifier.N7240d0e99(i);
    } 
    return p;
  }
  static double N5755eea72(Object []i) {
    double p = Double.NaN;
    if (i[29] == null) {
      p = 0;
    } else if (((Double) i[29]).doubleValue() <= 0.220704) {
      p = 0;
    } else if (((Double) i[29]).doubleValue() > 0.220704) {
    p = WekaClassifier.N4aa742bc3(i);
    } 
    return p;
  }
  static double N4aa742bc3(Object []i) {
    double p = Double.NaN;
    if (i[13] == null) {
      p = 0;
    } else if (((Double) i[13]).doubleValue() <= 3.495773) {
    p = WekaClassifier.N2b0e6aee4(i);
    } else if (((Double) i[13]).doubleValue() > 3.495773) {
      p = 2;
    } 
    return p;
  }
  static double N2b0e6aee4(Object []i) {
    double p = Double.NaN;
    if (i[30] == null) {
      p = 0;
    } else if (((Double) i[30]).doubleValue() <= 1.390871) {
    p = WekaClassifier.N3f8e45575(i);
    } else if (((Double) i[30]).doubleValue() > 1.390871) {
      p = 1;
    } 
    return p;
  }
  static double N3f8e45575(Object []i) {
    double p = Double.NaN;
    if (i[13] == null) {
      p = 1;
    } else if (((Double) i[13]).doubleValue() <= 0.701356) {
    p = WekaClassifier.N156a735a6(i);
    } else if (((Double) i[13]).doubleValue() > 0.701356) {
    p = WekaClassifier.N7fbb24b17(i);
    } 
    return p;
  }
  static double N156a735a6(Object []i) {
    double p = Double.NaN;
    if (i[1] == null) {
      p = 0;
    } else if (((Double) i[1]).doubleValue() <= 5.951814) {
      p = 0;
    } else if (((Double) i[1]).doubleValue() > 5.951814) {
      p = 1;
    } 
    return p;
  }
  static double N7fbb24b17(Object []i) {
    double p = Double.NaN;
    if (i[22] == null) {
      p = 3;
    } else if (((Double) i[22]).doubleValue() <= 0.19945) {
      p = 3;
    } else if (((Double) i[22]).doubleValue() > 0.19945) {
    p = WekaClassifier.N576621fa8(i);
    } 
    return p;
  }
  static double N576621fa8(Object []i) {
    double p = Double.NaN;
    if (i[8] == null) {
      p = 0;
    } else if (((Double) i[8]).doubleValue() <= 4.576393) {
      p = 0;
    } else if (((Double) i[8]).doubleValue() > 4.576393) {
      p = 3;
    } 
    return p;
  }
  static double N7240d0e99(Object []i) {
    double p = Double.NaN;
    if (i[16] == null) {
      p = 1;
    } else if (((Double) i[16]).doubleValue() <= 3.764393) {
    p = WekaClassifier.N7d53ab9810(i);
    } else if (((Double) i[16]).doubleValue() > 3.764393) {
    p = WekaClassifier.N6fd1233019(i);
    } 
    return p;
  }
  static double N7d53ab9810(Object []i) {
    double p = Double.NaN;
    if (i[31] == null) {
      p = 1;
    } else if (((Double) i[31]).doubleValue() <= 1.485314) {
    p = WekaClassifier.N26c87011(i);
    } else if (((Double) i[31]).doubleValue() > 1.485314) {
    p = WekaClassifier.N17a4756c17(i);
    } 
    return p;
  }
  static double N26c87011(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 1;
    } else if (((Double) i[0]).doubleValue() <= 108.920289) {
    p = WekaClassifier.N7231312312(i);
    } else if (((Double) i[0]).doubleValue() > 108.920289) {
      p = 1;
    } 
    return p;
  }
  static double N7231312312(Object []i) {
    double p = Double.NaN;
    if (i[14] == null) {
      p = 3;
    } else if (((Double) i[14]).doubleValue() <= 0.364894) {
      p = 3;
    } else if (((Double) i[14]).doubleValue() > 0.364894) {
    p = WekaClassifier.N7b912b6613(i);
    } 
    return p;
  }
  static double N7b912b6613(Object []i) {
    double p = Double.NaN;
    if (i[64] == null) {
      p = 1;
    } else if (((Double) i[64]).doubleValue() <= 3.152677) {
    p = WekaClassifier.N77b874ea14(i);
    } else if (((Double) i[64]).doubleValue() > 3.152677) {
    p = WekaClassifier.N6a7e826b16(i);
    } 
    return p;
  }
  static double N77b874ea14(Object []i) {
    double p = Double.NaN;
    if (i[5] == null) {
      p = 0;
    } else if (((Double) i[5]).doubleValue() <= 6.034756) {
    p = WekaClassifier.N6f03ec0d15(i);
    } else if (((Double) i[5]).doubleValue() > 6.034756) {
      p = 1;
    } 
    return p;
  }
  static double N6f03ec0d15(Object []i) {
    double p = Double.NaN;
    if (i[10] == null) {
      p = 1;
    } else if (((Double) i[10]).doubleValue() <= 1.513895) {
      p = 1;
    } else if (((Double) i[10]).doubleValue() > 1.513895) {
      p = 0;
    } 
    return p;
  }
  static double N6a7e826b16(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 2;
    } else if (((Double) i[0]).doubleValue() <= 101.33611) {
      p = 2;
    } else if (((Double) i[0]).doubleValue() > 101.33611) {
      p = 1;
    } 
    return p;
  }
  static double N17a4756c17(Object []i) {
    double p = Double.NaN;
    if (i[21] == null) {
      p = 3;
    } else if (((Double) i[21]).doubleValue() <= 1.462598) {
      p = 3;
    } else if (((Double) i[21]).doubleValue() > 1.462598) {
    p = WekaClassifier.N2e19119418(i);
    } 
    return p;
  }
  static double N2e19119418(Object []i) {
    double p = Double.NaN;
    if (i[1] == null) {
      p = 1;
    } else if (((Double) i[1]).doubleValue() <= 14.27809) {
      p = 1;
    } else if (((Double) i[1]).doubleValue() > 14.27809) {
      p = 2;
    } 
    return p;
  }
  static double N6fd1233019(Object []i) {
    double p = Double.NaN;
    if (i[12] == null) {
      p = 3;
    } else if (((Double) i[12]).doubleValue() <= 5.19769) {
      p = 3;
    } else if (((Double) i[12]).doubleValue() > 5.19769) {
      p = 0;
    } 
    return p;
  }
  static double Nb5d5daa20(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 2;
    } else if (((Double) i[0]).doubleValue() <= 979.551171) {
    p = WekaClassifier.N1eb049ba21(i);
    } else if (((Double) i[0]).doubleValue() > 979.551171) {
      p = 3;
    } 
    return p;
  }
  static double N1eb049ba21(Object []i) {
    double p = Double.NaN;
    if (i[1] == null) {
      p = 2;
    } else if (((Double) i[1]).doubleValue() <= 130.047754) {
    p = WekaClassifier.N47b8621322(i);
    } else if (((Double) i[1]).doubleValue() > 130.047754) {
    p = WekaClassifier.N3bd8c20433(i);
    } 
    return p;
  }
  static double N47b8621322(Object []i) {
    double p = Double.NaN;
    if (i[4] == null) {
      p = 2;
    } else if (((Double) i[4]).doubleValue() <= 73.528148) {
    p = WekaClassifier.N1a2ef22e23(i);
    } else if (((Double) i[4]).doubleValue() > 73.528148) {
    p = WekaClassifier.N3196322827(i);
    } 
    return p;
  }
  static double N1a2ef22e23(Object []i) {
    double p = Double.NaN;
    if (i[12] == null) {
      p = 1;
    } else if (((Double) i[12]).doubleValue() <= 2.446076) {
      p = 1;
    } else if (((Double) i[12]).doubleValue() > 2.446076) {
    p = WekaClassifier.N1db77024(i);
    } 
    return p;
  }
  static double N1db77024(Object []i) {
    double p = Double.NaN;
    if (i[24] == null) {
      p = 2;
    } else if (((Double) i[24]).doubleValue() <= 6.481975) {
    p = WekaClassifier.N1ef61a1f25(i);
    } else if (((Double) i[24]).doubleValue() > 6.481975) {
    p = WekaClassifier.N2f2cf91a26(i);
    } 
    return p;
  }
  static double N1ef61a1f25(Object []i) {
    double p = Double.NaN;
    if (i[19] == null) {
      p = 3;
    } else if (((Double) i[19]).doubleValue() <= 1.370042) {
      p = 3;
    } else if (((Double) i[19]).doubleValue() > 1.370042) {
      p = 2;
    } 
    return p;
  }
  static double N2f2cf91a26(Object []i) {
    double p = Double.NaN;
    if (i[1] == null) {
      p = 2;
    } else if (((Double) i[1]).doubleValue() <= 52.255669) {
      p = 2;
    } else if (((Double) i[1]).doubleValue() > 52.255669) {
      p = 3;
    } 
    return p;
  }
  static double N3196322827(Object []i) {
    double p = Double.NaN;
    if (i[21] == null) {
      p = 2;
    } else if (((Double) i[21]).doubleValue() <= 17.615123) {
    p = WekaClassifier.N7bb6f98628(i);
    } else if (((Double) i[21]).doubleValue() > 17.615123) {
    p = WekaClassifier.N60331d8b32(i);
    } 
    return p;
  }
  static double N7bb6f98628(Object []i) {
    double p = Double.NaN;
    if (i[27] == null) {
      p = 2;
    } else if (((Double) i[27]).doubleValue() <= 13.780701) {
    p = WekaClassifier.N29b80bde29(i);
    } else if (((Double) i[27]).doubleValue() > 13.780701) {
    p = WekaClassifier.N4a044c6331(i);
    } 
    return p;
  }
  static double N29b80bde29(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 2;
    } else if (((Double) i[0]).doubleValue() <= 624.245557) {
    p = WekaClassifier.N7213313730(i);
    } else if (((Double) i[0]).doubleValue() > 624.245557) {
      p = 2;
    } 
    return p;
  }
  static double N7213313730(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 2;
    } else if (((Double) i[0]).doubleValue() <= 587.615376) {
      p = 2;
    } else if (((Double) i[0]).doubleValue() > 587.615376) {
      p = 3;
    } 
    return p;
  }
  static double N4a044c6331(Object []i) {
    double p = Double.NaN;
    if (i[5] == null) {
      p = 3;
    } else if (((Double) i[5]).doubleValue() <= 35.175041) {
      p = 3;
    } else if (((Double) i[5]).doubleValue() > 35.175041) {
      p = 2;
    } 
    return p;
  }
  static double N60331d8b32(Object []i) {
    double p = Double.NaN;
    if (i[12] == null) {
      p = 2;
    } else if (((Double) i[12]).doubleValue() <= 27.143165) {
      p = 2;
    } else if (((Double) i[12]).doubleValue() > 27.143165) {
      p = 3;
    } 
    return p;
  }
  static double N3bd8c20433(Object []i) {
    double p = Double.NaN;
    if (i[13] == null) {
      p = 3;
    } else if (((Double) i[13]).doubleValue() <= 22.085303) {
      p = 3;
    } else if (((Double) i[13]).doubleValue() > 22.085303) {
      p = 2;
    } 
    return p;
  }
}
