function predLabels = cosineClassifier(dataFile)

clc;
disp('load data...');

load(dataFile);
%load('/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics-1.mat');
%load('/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-rec.motorcycles-1.mat');
disp('load data finished.');

labelNum = size(labelData, 2)
for i = 1:labelNum
    disp(i);
    sample = labelData{i};
    indices = sample{2};
    values = sample{3};
    
    vsize = size(indexMap, 1);
    vector = sparse(vsize, 1);
    for j = 1:size(indices)
        indexNew = indexMap(indices(j));
        vector(indexNew) = values(j);
    end
    
    labelMat(i) = {vector};
    labelArray(i) = sample{1};
end

docNum = size(docData, 2)
for i = 1:docNum
    if mod(i, 100) == 0
        disp(['processed ', num2str(i)]);
    end
    sample = docData{i};
    indices = sample{2};
    values = sample{3};
    
    vsize = size(indexMap, 1);
    vector = sparse(vsize, 1);
    for j = 1:size(indices)
        indexNew = indexMap(indices(j));
        vector(indexNew) = values(j);
    end
    
    dataMat(i) = {vector};
    dataLabelArray(i) = sample{1};
end

correct = 0;
for i = 1: docNum
  
    if mod(i, 100) == 0
        disp(['classified ', num2str(i)]);
    end
     
    dvector = dataMat{i};
    maxValue = 0;
    label = '';
    for j = 1:labelNum
        lvector = labelMat{j};
        
        cosValue = dot(dvector,lvector)/(1E-10 + norm(dvector)*norm(lvector));
        
        if cosValue > maxValue
            maxValue = cosValue;
            label = labelArray{j};
        end
    end
    
    predLabels{i} = {label, maxValue};
    
    if strcmp(label, dataLabelArray{i}) == 1
        correct = correct + 1;
    end
end

correct/docNum


end