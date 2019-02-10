function allData = readFolder(folderName, dimension)
% Search for number of string matches per line.  

files = dir(fullfile(folderName, '/*.txt'));

fnum = size(files, 1);


for i = 1:fnum
    files(i).name
    sample = readFile([folderName, '/', files(i).name], dimension);
    
    allData(i) = {sample};
end

end