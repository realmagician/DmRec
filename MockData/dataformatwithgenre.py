fin = file('u.item', 'r')
fout = file('uMahout_withgenre.data', 'w')
while True:
	line = fin.readline()
	if len(line) == 0:
		break
	line = line.strip()
	strs = line.split('|')
	if len(strs) < 6:
		continue
	for i in range(5, len(strs)):
		newline = '%s,%s,%s\n' % (strs[0], i-5 , strs[i])
		fout.write(newline)
fin.close()
fout.close()