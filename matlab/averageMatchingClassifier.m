function [  ] = maxMatchingClassifier( dataFile );

clc;

disp('load data...');

%load(dataFile);
load('/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics-1-new.mat');
%load('/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-rec.motorcycles-1-new.mat');
disp('load data finished.');


labelNum = size(labelData, 2)
docNum = size(docData, 2)
correct = 0;
for i = 1: docNum
    doc = docData{i};
    
    docIds = doc{2};
    docWeights = doc{3};
    docMat = doc{4};
    docNorm = norm(docWeights);
    docSize = size(docIds);
    for j = 1:docSize
        docVecNorm(j) = norm(docMat(j,:));
    end
    
    maxValue = 0;
    label = '';
    for j = 1:labelNum
        lab = labelData{j};
        
        labelIds = lab{2};
        labelWeights = lab{3};
        labelMat = lab{4};

        labelNorm = norm(labelWeights);
        labelSize = size(labelIds);
        for k = 1:labelSize
            labelVecNorm(k) = norm(labelMat(k,:));
        end

        weightMat = docVecNorm' * labelVecNorm + 1E-100;
        confMat = docMat * labelMat';
        confMatNormed = (confMat) ./ (weightMat);
        
      
        weighMat = docWeights * labelWeights';

        confMatNew = exp((confMatNormed - 1)/0.03) .* weighMat;
        
        sim = sum(sum(confMatNew));
        
        simValue = sim / labelNorm / docNorm;
        
        if simValue > maxValue
            maxValue = simValue;
            label = lab{1};
        end
        
        
    end
    
    predLabels{i} = {label, maxValue};
    
    if strcmp(label, doc{1}) == 1
        correct = correct + 1;
    end
    
    if mod(i, 100) == 0
        disp(['classified ', num2str(i)]);
    end

end

correct/docNum
end


