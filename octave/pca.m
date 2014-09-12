function [S] = pca(batch1, batch2, batch15, batch16)

a=batch1';
b=batch2';
x=batch15';
y=batch16';

xy=[a;b;x;y];
sigma=xy'*xy;
sigma=sigma./size(xy,1);
[U,S,V]=svd(sigma);

Z1=a*U(:,1:2);
Z2=b*U(:,1:2);
Z3=x*U(:,1:2);
Z4=y*U(:,1:2);
Zall=[Z1;Z2;Z3;Z4];

palette=hsv(5);
colors=[ones(size(Z1,1),3).*palette(1,:);
	ones(size(Z2,1),3).*palette(2,:);
	ones(size(Z3,1),3).*palette(3,:);
	ones(size(Z4,1),3).*palette(4,:)];
scatter(Zall(:,1),Zall(:,2),8,colors);


