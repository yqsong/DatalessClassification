function [ predLabels ] = hungarianMatchingClassifier( dataFile )
%HUNGARIANMATCHINGCLASSIFIER Summary of this function goes here
%   Detailed explanation goes here

clc;

javaaddpath('/shared/shelley/yqsong/workspace/descartes-release-yq/matlab/hungarian.jar');
clear java;
import optimization.*;
methods('HungarianAlgorithm');

disp('load data...');

load(dataFile);
%'/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-sci.electronics-1-new.mat'
%load('/shared/shelley/yqsong/data/20ngSim/outputMatlab/rec.autos-rec.motorcycles-1-new.mat');
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
        weightMat = docVecNorm' * labelVecNorm + 1E-20;
        
        
        confMatNormedCache = (confMat) ./ (weightMat);
        
        confMatNormed = 1 - (confMat) ./ (weightMat);

        % These two are slow
        %[assignment,cost] = munkres(confMatWeighted);
        %%[assignment,cost] = hungarian1(confMatWeighted);

        % This is exported from Java
        hangarian = HungarianAlgorithm(confMatNormed);
        assignment = hangarian.execute() + 1;
        assignmentNum = size(assignment, 1);

        sim = 0;
        for k = 1:assignmentNum
%             k
%             confMatWeightedCache(k, assignment(k))
            if (confMatNormedCache(k, assignment(k)) > threshold) 
                
                sim = sim + confMatNormedCache(k, assignment(k)) * docWeights(k) * labelWeights(assignment(k));
            end
        end

        simValue = sim / labelNorm / docNorm;
        
        if simValue > maxValue
            maxValue = simValue;
            label = lab{1};
        end
        
        predLabels{i} = {label, maxValue};
    end
    if strcmp(label, doc{1}) == 1
        correct = correct + 1;
    end
    
    if mod(i, 10) == 0
        disp(['classified ', num2str(i)]);
    end

end

correct/docNum
end


