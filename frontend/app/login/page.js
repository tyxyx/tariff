import Link from "next/link";
import { Button } from "@/components/ui/button";
import {LoginForm, LoginInput} from "@/components/login-form";
import { colors } from "@/styles/colors";

export default function LoginPage(){
    return(
        <div
          className="min-h-screen flex justify-center items-center"
          style={{ backgroundColor: colors.background, color: colors.text }}
        >
          {/*Form */}
          <section
        className="py-24 px-6 w-full max-w-lg bg-card rounded-lg shadow-lg"
        style={{ backgroundColor: colors.card}}
      >
        <main>
          <h2 className="text-6xl font-semibold mb-6">Login</h2>
          <h4 className="text-3xl font-semibold mb-6" style={{ color: colors.mutedText }}>Welcome to TariffCalc Pro</h4>
            <LoginForm>
              <LoginInput>
                Username or Email
              </LoginInput>
              <LoginInput>
                Password
              </LoginInput>
            </LoginForm>
          </main>
        
      </section>
          
        </div>
    )
}