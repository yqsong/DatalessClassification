function [ predLabels ] = maxMatchingClassifier( dataFile );

clc;

disp('load data...');

%load(dataFile);
%load('/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics-1-new.mat');
load('/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-rec.motorcycles-1-new.mat');
disp('load data finished.');

threshold = 0.85;

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

        confMat = docMat * labelMat';
        weightMat = docVecNorm' * labelVecNorm + 1E-100;
        
        
        confMatNormed = (confMat) ./ (weightMat);
        
        % using label to match doc
        [match1,assignment] = max(confMatNormed,[],1);
        
        assignmentNum = length(assignment);
        sim1 = 0;
        for k = 1:assignmentNum
            if (confMatNormed(assignment(k), k) > threshold) 
                
                sim1 = sim1 + confMatNormed(assignment(k), k) * docWeights(assignment(k)) * labelWeights(k);
            end
        end

        % using doc to match label
        [match2,assignment] = max(confMatNormed,[],2);
        
        assignmentNum = length(assignment);
        sim2 = 0;
%         for k = 1:assignmentNum
%             if (confMatNormed(k, assignment(k)) > threshold) 
%                 
%                 sim2 = sim2 + confMatNormed(k, assignment(k)) * docWeights(k) * labelWeights(assignment(k));
%             end
%         end

        
        simValue = (sim1 + sim2) / labelNorm / docNorm;
        
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


