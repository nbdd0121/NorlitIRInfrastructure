package io.github.nbdd0121.compiler.ir.parser;

import io.github.nbdd0121.compiler.backend.Backend;
import io.github.nbdd0121.compiler.backend.CodeGenerator;
import io.github.nbdd0121.compiler.backend.x64.X64Backend;
import io.github.nbdd0121.compiler.ir.Module;
import io.github.nbdd0121.compiler.ir.optimze.BlockGarbageCollection;
import io.github.nbdd0121.compiler.ir.optimze.CopyPropagation;
import io.github.nbdd0121.compiler.ir.optimze.DeadCodeElimination;
import io.github.nbdd0121.compiler.ir.optimze.LoadStoreOptimization;
import io.github.nbdd0121.compiler.ir.optimze.LocalVarRenaming;
import io.github.nbdd0121.compiler.ir.optimze.NopElimination;
import io.github.nbdd0121.compiler.ir.optimze.PartialConstantFolding;
import io.github.nbdd0121.compiler.ir.pass.BlockNormalizePass;
import io.github.nbdd0121.compiler.ir.pass.TypeCheckingPass;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.nwgjb.commons.io.FileReaderUtil;

public class Test {

	public static void main(String[] args) throws IOException {
		byte[] file = FileReaderUtil.readFullyAsByteArray("test.nir");
		Tokenizer t = new Tokenizer(new ByteArrayInputStream(file));
		
		Module mod = Parser.parse(t);
		
		
		new TypeCheckingPass().pass(mod);
		
		new BlockNormalizePass().pass(mod);
		
		if (true) {
			boolean optimized = true;
			while (optimized) {
				optimized = false;
				
				optimized |= new LoadStoreOptimization().pass(mod);
				optimized |= new PartialConstantFolding().pass(mod);
				optimized |= new DeadCodeElimination().pass(mod);
				optimized |= new CopyPropagation().pass(mod);
				optimized |= new NopElimination().pass(mod);
			}
			new LocalVarRenaming().pass(mod);
		}
		
		// new IntoSSAPass().pass(mod);
		
		new BlockGarbageCollection().pass(mod);
		
		Backend.setBackend(X64Backend.getInstance());
		CodeGenerator.newInstance().generate(mod);

//		for (Declaration d : mod.getDeclarations()) {
//			if (d instanceof Function) {
//				System.out.println(DotDump.dump((Function) d));
//			}
//		}
		System.out.println(mod);
	}

}
