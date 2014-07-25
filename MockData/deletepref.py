fin = file('uMahout.data', 'r')
fout = file('uMahout_extr.data', 'w')
while True:
	line = fin.readline()
	if len(line) == 0:
		break
	strs = line.split(',')
	if len(strs) < 3:
		continue
	newline = '%s,%s,1\n' % (strs[0], strs[1])
	fout.write(newline)
fin.close()
fout.close()